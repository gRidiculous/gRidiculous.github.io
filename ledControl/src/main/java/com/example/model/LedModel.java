package com.example.model;

import org.springframework.stereotype.Component;

import lombok.Data;


@Component
@Data
public class LedModel {
	private String din; 
	private String ledIp;
	private int ledPort;
	private int area;
	private int alterarea;
	private int effects;
	private int Kaoqineffects;
	
}
