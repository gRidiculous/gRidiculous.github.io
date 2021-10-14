package com.example.ledControl.model.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ledControl.model.service.MqttService;
import com.example.model.LedModel;
import com.example.model.MapModel;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;

@Component
public class MyApplicationRunner implements ApplicationRunner{

	@Resource
	private MqttService MqttService;
	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		MapModel.din="300000000000060023";
		String fileName = System.getProperty("user.dir") + "//json//devices.json";
		File jsonFile = new File(fileName);
		boolean res=false;
		if (jsonFile.length()<1) {
			 res = getconfig(MapModel.din);
		} else {
			res=true;
		}
		if (res) {			
			String jsonStr = "";
			FileReader fileReader = new FileReader(jsonFile);
			Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
			int ch = 0;
			StringBuffer sb = new StringBuffer();
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
			fileReader.close();
			reader.close();
			jsonStr = sb.toString();
			JSONArray jobj = JSON.parseArray(jsonStr);
			for (int i = 0; i < jobj.size(); i++) {
				JSONObject jsonObject = jobj.getJSONObject(i);
				if (jsonObject.getIntValue("devicesType") == 34) {
					String dinNameString = jsonObject.getString("name");
					MapModel.projectId = jsonObject.getString("projectId");
					System.out.println(
							"网关别名:" + dinNameString + " din码:" + MapModel.din + " 项目id:" + MapModel.projectId);

				}
				else if (jsonObject.getIntValue("devicesType") == 51) {
					LedModel led = new LedModel();
					MapModel.ledDin=jsonObject.getString("din");
					led.setDin(jsonObject.getString("din"));
					led.setLedIp(jsonObject.getString("ledIp"));
					led.setLedPort(jsonObject.getInteger("ledPort"));
					led.setArea(jsonObject.getInteger("area"));
					led.setAlterarea(jsonObject.getInteger("alterarea"));
					led.setEffects(jsonObject.getInteger("effects"));
					led.setKaoqineffects(jsonObject.getInteger("KaoQineffects"));
					MapModel.ledDinMap.put(led.getDin(), led);
					System.out.println(led.toString());
				}
				else if(jsonObject.getIntValue("devicesType") == 52){
					MapModel.hqFaceList.add(jsonObject.getString("din"));
					System.out.println(jsonObject.getString("din"));
				}

			}
		}
		MqttService.setClientid("ledControl" + UUID.randomUUID().toString());
		MqttService.start();
	}
	
public boolean getconfig(String dinId) throws Exception {
		
		boolean res = false;
		String path = System.getProperty("user.dir") + "//json";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("din", dinId);
		String result2 = HttpRequest.post("https://site.weiotchina.cn:9076/device/configjson")
				.header(Header.AUTHORIZATION, "wangguanzhuanyong")// 头信息，多个头信息多次调用此方法即可
				.form(param).timeout(20000)// 超时，毫秒
				.execute().body();
		JSONObject jsonResult = JSONObject.parseObject(result2);
		JSONArray jsonArray = new JSONArray();
		jsonArray = jsonResult.getJSONArray("data");
		if (jsonArray.size() > 0) {
			System.out.println("接收到的配置文件:" + jsonArray.toJSONString());
			res = CreateJsonFileUtils.createJsonFile(jsonArray.toJSONString(), path, "devices");
		}
		return res;

	}
	

}
