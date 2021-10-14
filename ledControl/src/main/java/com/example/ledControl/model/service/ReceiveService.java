package com.example.ledControl.model.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.example.ledControl.mapper.CarMapper;
import com.example.model.LedModel;
import com.example.model.MapModel;

@Service
public class ReceiveService {

//	int carNowTotal=5;
	@Resource
	private LedService ledService;
	@Resource
	private ThreadPoolTaskExecutor asyncService;
	
	@Resource
	private CarMapper carMapper;

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(ReceiveService.class);

	public void getTask(String topic, String body) {
		try {
			if (topic.indexOf("Rec") != -1) {
				JSONObject jsonDate = JSONObject.parseObject(body);
				JSONObject jsonSencondDateJson = jsonDate.getJSONObject("info");
				String personId = jsonSencondDateJson.getString("customId");
				String faceId = jsonSencondDateJson.getString("facesluiceId");
				String crossTime = jsonSencondDateJson.getString("time");
				String personName = jsonSencondDateJson.getString("persionName");
				String direction = jsonSencondDateJson.getString("direction");
				String derection = "出场";
				if (direction.equals("entr")) {
					derection = "进场";
				}
				if (MapModel.hqFaceList != null && MapModel.hqFaceList.contains(faceId)) {
					LedModel led = MapModel.ledDinMap.get(MapModel.ledDin);
					if (led != null) {
						String content = personName + " " + crossTime + " " + derection;
						ledService.send(led.getLedIp(), led.getLedPort(), content, led.getArea(), led.getEffects());
					}
				}
			} else if (topic.indexOf("/weiotchina/msg/receive/v1/") != -1) {
				JSONObject jsonDate = JSONObject.parseObject(body);
				JSONObject jsonSencondDate = jsonDate.getJSONObject("data");
				String content = jsonSencondDate.getString("content");
				int area = jsonSencondDate.getIntValue("region");
				int effects = jsonSencondDate.getIntValue("effects");
				if (MapModel.ledDinMap.values() != null) {
					for (LedModel led : MapModel.ledDinMap.values()) {
						ledService.send(led.getLedIp(), led.getLedPort(), content, area, effects);
					}
				}
			}

			else if (topic.contains("car/300000000000060023")) {
				
				JSONObject jsonDate = JSONObject.parseObject(body);
				String cardNo = jsonDate.getString("CarNo");
				String RecordTime = jsonDate.getString("RecordTime");
				int PassType = jsonDate.getIntValue("PassType");
				double Fee = jsonDate.getDoubleValue("Fee");
				String CarType = jsonDate.getString("CarType");
				String direction = "出场";
				long time=new Date().getTime();
				if (PassType == 0) {
					direction = "进场";
					List<JSONObject>carList=carMapper.carList(cardNo);
					if (carList.isEmpty()) {
						carMapper.insertCar(cardNo, time, direction);
					}else {
						carMapper.updateCarin(cardNo);
					}
				}
				else if (PassType==1) {
					List<JSONObject>carList=carMapper.carList(cardNo);
					if (!carList.isEmpty()) {
						carMapper.updateCar(cardNo);
					}
				}
				if (MapModel.ledDinMap.values() != null) {
					for (LedModel led : MapModel.ledDinMap.values()) {
						String carNow = cardNo + " " + RecordTime + " " + direction;
						ledService.send(led.getLedIp(), led.getLedPort(), carNow, 4, 0);
						Thread.sleep(2000);
						int carTotal=carMapper.carTotal();
						String carTongji = "实时在场车辆总数:"+carTotal+"辆";
						ledService.send(led.getLedIp(), led.getLedPort(), carTongji, 3, 0);
						

					}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
