package com.example.ledControl.model.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ledControl.model.service.LedService;
import com.example.ledControl.model.service.MqttService;
import com.example.model.LedModel;
import com.example.model.MapModel;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;


@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling
public class ScheduleTask {
	private final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);

	@Resource
	private LedService LedService;
	
	@Resource
	private MqttService mqttService;

	private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	// 3.添加定时任务
//	@Scheduled(cron = "0 */1 * * * ?")
//	public void configureTasks() {
//		try {
//			Long nowTime = new Date().getTime();
//			String timeString2 = new SimpleDateFormat(TIME_FORMAT).format(nowTime);
//			JSONObject reJson = new JSONObject();
//			reJson.put("datapointID", 20000001);
//			reJson.put("childList", "");
//			reJson.put("timeStamp", nowTime);
//			reJson.put("time", timeString2);
//			reJson.put("Type", "heartbeat");
//			reJson.put("din", MapModel.din);
//			mqttService.sendMessageHeartbeat(reJson.toJSONString());
//
//		} catch (
//
//		Exception e) {
//			// TODO: handle exception
//			logger.info(e.toString());
//		}
//	}

	@Scheduled(fixedRate = 120000)
	public void KaoQinEffecte() {
		try {
			if (MapModel.ledDinMap.values() != null) {
				for (LedModel led : MapModel.ledDinMap.values()) {
					// 链式构建请求
					JSONObject objectPrObject = new JSONObject();
					// string
					objectPrObject.put("projectId", MapModel.projectId);
					String result2 = HttpRequest.post("https://site.weiotchina.cn:9076/worker/usertype")
							.header(Header.AUTHORIZATION, "wangguanzhuanyong")// 头信息，多个头信息多次调用此方法即可
							.header(Header.CONTENT_TYPE, "application/json").body(objectPrObject.toJSONString())
							.timeout(20000)// 超时，毫秒
							.execute().body();

					JSONObject jsonResult = JSONObject.parseObject(result2);
					JSONArray jsonArray = new JSONArray();
					jsonArray = jsonResult.getJSONArray("data");
					StringBuffer dataBuffer = new StringBuffer();
					if (jsonArray.size() > 0) {
						for (int i = 0; i < jsonArray.size(); i++) {
							dataBuffer.append(jsonArray.getJSONObject(i).get("name")).append(":")
									.append(jsonArray.getJSONObject(i).get("value")).append("人   ");

						}
						LedService.send(led.getLedIp(), led.getLedPort(), dataBuffer.toString(), led.getAlterarea(),
								led.getKaoqineffects());
//						logger.info(dataBuffer.toString());
					} else {
						LedService.send(led.getLedIp(), led.getLedPort(), "该项目无人员类别统计", led.getAlterarea(),
								led.getKaoqineffects());
						logger.info("该项目无人员类别统计");
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
