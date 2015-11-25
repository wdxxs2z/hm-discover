package com.cloudpass.hm.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class MarathonEventBusController {
	
	@RequestMapping(value="event_callback",method=RequestMethod.POST)
	@ResponseBody
	public String eventCallback(HttpServletRequest request) throws IOException{
		ServletInputStream inputStream = request.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        int i = -1; 
        while((i=inputStream.read())!=-1){ 
        baos.write(i); 
        } 
       System.out.println(baos.toString());
       return "event_callback";
	}

}
