package com.controller;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import com.Main;
import com.course.CourseWebObserver;
import com.stu.StuModel;
import com.stu.StuModelObservable;
import com.view.CourseRegPane;

public class CourseRegController implements CourseRegControllerInterface {
	private CourseRegPane component;
	private StuModel model;
	private DefaultListModel<String> listModel;
	private Map<String, String> infoMap;
	private Map<String, String> listInfoMap;
	private DateFormat mediumFormat;
	private Main main;
	
	private static final int MAX_RELOGIN_NUM = 3;
	private int reloginCounter = 0;
	
	private static final String[] MSG_RESULT = new String[] 
			{"�������~", "�[�令�\", "���ƥ[��", "�ҵ{�İ�", "���ŦX����", "�H�Ƥw��", "�䤣��ҵ{", "�ҵ{�N�X���~", "�s�u���~"};
	
	public CourseRegController(Main main, StuModel model) {
		this.main = main;
		this.model = model;
		initialize();
	}
	
	private void initialize() {
		component = new CourseRegPane(this);
		listModel = component.getDefaultListModel();
		infoMap = new TreeMap<String, String>();
		listInfoMap = new TreeMap<String, String>();
		mediumFormat = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.MEDIUM);
		
		
		model.registerObserver((StuModelObservable)this);
		
		String info = formatString("�ҵ{�N�X", 12) + formatString("�ҵ{�W��", 15) + 
				formatString("�½ұЮv", 15) + formatString("�ثe��ҤH��", 14) + 
				formatString("������ҤH�ƤW��", 13) + formatString("�ثe���A", 15) +
				formatString("�̫��s�ɶ�", 15);
		
		
		
		listModel.addElement(info);
		
	}

	@Override
	public void setCourseRegResult(String courseCode, int result) {
		String status = (result > 0 && result <= 7) ? MSG_RESULT[result] :  MSG_RESULT[0];
		String info = infoMap.get(courseCode);
		addListElement(courseCode, info, status);
	}

	@Override
	public void update(String courseCode, Map<String, String> dataMap) {
		String courseName = dataMap.get(CourseWebObserver.FIELD_COURSE_NAME);
		String lecturer = dataMap.get(CourseWebObserver.FIELD_LECTURER);
		String currentRegNum = dataMap.get(CourseWebObserver.FIELD_CURRENT_REG_NUM);
		String maxRegNum = dataMap.get(CourseWebObserver.FIELD_MAX_REG_NUM);
		
		String info = formatString(courseCode, 15) + formatString(courseName, 15) + 
				formatString(lecturer, 15) + formatString(currentRegNum, 15) + 
				formatString(maxRegNum, 15);

		addListElement(courseCode, info, "���椤");
	}
	
	
	private String formatString(String s, int length) {
		s = (s == null) ? "null" : s;
		StringBuilder sb = new StringBuilder(s);
		int offset = length - s.length();
		for(int i = 0; i < offset; i++)
			sb.append("�@");
		return sb.toString();
	}

	@Override
	public boolean addWaitCourse(String courseCode) {
		if(courseCode.length() != 9) {
			JOptionPane.showMessageDialog(component, "�ҵ{�N�X���~!", null, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(infoMap.containsKey(courseCode)) {
			JOptionPane.showMessageDialog(component, "�ҵ{" + courseCode + "�w�b�Ƶ{��", null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		model.addWaitCourse((StuModelObservable)this, courseCode);
		return true;
	}

	@Override
	public boolean removeWaitCourse(String courseCode) {
		if(!infoMap.containsKey(courseCode)) {
			JOptionPane.showMessageDialog(component, "�ҵ{" + courseCode + "���b�Ƶ{��", null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		boolean result = model.removeWaitCourse((StuModelObservable)this, courseCode);
		removeListElment(courseCode);
		return result;
	}
	

	@Override
	public Component getComponent() {
		return component;
	}
	
	
	private boolean removeListElment(String courseCode) {
		if(!infoMap.containsKey(courseCode))
			return false;
		String s = listInfoMap.get(courseCode);
		listModel.removeElement(s);
		infoMap.remove(courseCode);
		listInfoMap.remove(courseCode);
		return true;
	}
	
	private void addListElement(String courseCode, String element, String status) {
		String line = element + formatString(status, 15) + formatString(mediumFormat.format(
				System.currentTimeMillis()), 30);
		
		if(listInfoMap.containsKey(courseCode)) {
			String listInfo = listInfoMap.get(courseCode);
			int index = listModel.indexOf(listInfo);
			listModel.remove(index);
			listModel.add(index, line);
		} else {
			listModel.addElement(line);
			
		}
		infoMap.put(courseCode, element);
		listInfoMap.put(courseCode, line);
	}

	/*
	@Override
	synchronized public void updateLoginState() {
		if(model.isReady())
			return;
		
		//JOptionPane.showMessageDialog(component, "���s�n�J", null, JOptionPane.WARNING_MESSAGE);
		int confirmResult = JOptionPane.showConfirmDialog(component, "�Э��s�n�J", null, 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(confirmResult != JOptionPane.OK_OPTION) {
			main.setLoginView();
			return;
		}
		
		int result = model.getReady();
		if(result == StuModel.MSG_SUCCESS)
			return;
		if(result == StuModel.MSG_NOT_CHOICE_TIME) {
			JOptionPane.showMessageDialog(component, "�{�b�w�O�D��Үɬq", null, JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(result == StuModel.MSG_LOGIN_FAIL) {
			JOptionPane.showMessageDialog(component, "�n�J����", null, JOptionPane.WARNING_MESSAGE);
			updateLoginState();
			return;
		}
		JOptionPane.showMessageDialog(component, "�������~", null, JOptionPane.ERROR_MESSAGE);
	}
	*/
	
	synchronized public void updateLoginState() {
		if(model.isReady())
			return;
		
		if(reloginCounter >= MAX_RELOGIN_NUM) {
			System.out.println("�w�F�۰ʭ��n���ƤW��!");
			reloginCounter = 0;
			main.setLoginView();
			return;
		}
		
		reloginCounter++;
		int result = model.getReady();
		if(result == StuModel.MSG_SUCCESS) {
			reloginCounter = 0;
			return;
		}
		if(result == StuModel.MSG_NOT_CHOICE_TIME) {
			JOptionPane.showMessageDialog(component, "�{�b�w�O�D��Үɬq", null, JOptionPane.WARNING_MESSAGE);
			reloginCounter = 0;
			main.setLoginView();
			return;
		}
		if(result == StuModel.MSG_LOGIN_FAIL) {
			System.out.println("�n�J���ѡA�A�����s�n�J�C");
			updateLoginState();
			return;
		}
		JOptionPane.showMessageDialog(component, "�������~", null, JOptionPane.ERROR_MESSAGE);
	}
}
