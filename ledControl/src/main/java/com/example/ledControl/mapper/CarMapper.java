package com.example.ledControl.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.alibaba.fastjson.JSONObject;

@Mapper
public interface CarMapper {

	@Insert("INSERT  INTO car VALUES(NULL,#{carNum},#{time},#{derection})")
	public int insertCar(String carNum,long time,String derection);
	
	@Select("SELECT * FROM car WHERE carNum=#{carNum} ORDER BY time DESC")
	public List<JSONObject>carList(String carNum);
	
	@Select("SELECT COUNT(*) FROM car WHERE derection='进场' ")
	public int carTotal();
	
	@Update("UPDATE car SET derection='出场'  WHERE carNum=#{carNum}")
	public int updateCar(String carNum);
	
	@Update("UPDATE car SET derection='进场'  WHERE carNum=#{carNum}")
	public int updateCarin(String carNum);
}
