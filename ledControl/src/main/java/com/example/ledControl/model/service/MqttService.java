package com.example.ledControl.model.service;

import java.util.Date;

import javax.annotation.Resource;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.example.model.MapModel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Data
public class MqttService {

	@Resource
	private ThreadPoolTaskExecutor asyncService;

	@Resource
	private ReceiveService ReceiveService;

	@Value("${spring.mqtt.host}")
	private String host;

	private String clientid;
	private MqttTopic topicPushHeartbeaTopic;

	@Value("${spring.mqtt.username}")
	private String userName;

	@Value("${spring.mqtt.password}")
	private String passWord;

	@Value("${spring.mqtt.topicHeartbeat}")
	private String topicHeartbeat;
	@Value("${spring.mqtt.receive}")
	private String receive;
	@Value("${spring.mqtt.receive1}")
	private String receive1;

	@Value("${spring.mqtt.receivepost}")
	private String receivepost;

	private MqttClient client;
	private MqttConnectOptions options = new MqttConnectOptions();

	private boolean isConnecting = false;

	// 接收的消息序号
	static String seq = "";

	public MqttClient getClient() {
		return client;
	}

	public MqttService() {

	}

	public void start() {
		log.info("mqtt info :{}", this.toString());
		if (initClient()) {
			connect();
		}
	}

	private boolean initClient() {
		options.setCleanSession(false);
		options.setUserName(userName);
		options.setPassword(passWord.toCharArray());
		options.setConnectionTimeout(10);
		options.setKeepAliveInterval(20);
		options.setMaxInflight(100);
		try {
			client = new MqttClient(host, clientid, new MemoryPersistence());
			topicPushHeartbeaTopic = client.getTopic(topicHeartbeat);

			;
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			log.error("创建mqtt对象失败: " + e1);
			return false;
		}

		client.setCallback(new MqttCallbackExtended() {
			int count = 0;

			@Override
			public void connectionLost(Throwable cause) {
				log.error("Lost connection!!! {}");
				connect();
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				log.debug("send success ? --> {}, {}", token.isComplete(), count++);
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
//				log.info("接收消息主题 : " + topic);
//				log.info("接收消息内容 : " + new String(message.getPayload()));

				asyncService.execute(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							ReceiveService.getTask(topic, new String(message.getPayload()));

						} catch (Exception e) {
							log.error("消息处理异常：" + e.getMessage());
						}
					}

				});
			}

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				// TODO Auto-generated method stub
				log.info("mqtt 己连接");
				int[] qos = { 1, 1 ,1};
				String[] serverTopic = { receive,receive1, receivepost + MapModel.din };
				try {
					client.subscribe(serverTopic, qos);
					log.info("mqtt 己订阅主题: " + receive + " "+receive1+" " + receivepost + MapModel.din);
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					log.error("mqtt 订阅主题失败: " + e);
				}
			}

		});

		return true;
	}

	// 连接
	private void connect() {
		try {
			// 建立连接
			if (null != client && !client.isConnected()) {
				client.connect(options);
			}

		} catch (Exception e) {

			log.error(new Date() + "连接mqtt失败: " + e);
			try {
				log.info("-----10秒后尝试连接-----");
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connect();

		}
	}

	private void publish(MqttTopic topic, MqttMessage message) {
		try {
			if (topic != null && message != null) {
				MqttDeliveryToken token = topic.publish(message);
				token.waitForCompletion(1000);
				if (token.isComplete() != true) {
					log.info("message is  not published completely! " + message.toString());
				} else {

					log.info("message is  published completely! " + message.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		}
	}

	public void sendMessageHeartbeat(String msg) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(1); // 保证消息能到达一次
			message.setRetained(false);
			message.setPayload(msg.getBytes());
			publish(topicPushHeartbeaTopic, message);
		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.toString());
		}

	}

}
