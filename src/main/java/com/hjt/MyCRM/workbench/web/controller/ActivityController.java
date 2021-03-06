package com.hjt.MyCRM.workbench.web.controller;

import com.hjt.MyCRM.exception.ActivityDeleteException;
import com.hjt.MyCRM.exception.ActivityModifyException;
import com.hjt.MyCRM.exception.ActivityRemarkDeleteException;
import com.hjt.MyCRM.exception.ActivitySaveException;
import com.hjt.MyCRM.settings.domain.User;
import com.hjt.MyCRM.settings.service.UserService;
import com.hjt.MyCRM.settings.service.impl.UserServiceImpl;
import com.hjt.MyCRM.utils.DateTimeUtil;
import com.hjt.MyCRM.utils.PrintJson;
import com.hjt.MyCRM.utils.ServiceFactory;
import com.hjt.MyCRM.utils.UUIDUtil;
import com.hjt.MyCRM.vo.PaginationVo;
import com.hjt.MyCRM.workbench.domain.Activity;
import com.hjt.MyCRM.workbench.domain.ActivityRemark;
import com.hjt.MyCRM.workbench.service.ActivityService;
import com.hjt.MyCRM.workbench.service.impl.ActivityServiceImpl;

import javax.print.attribute.HashPrintJobAttributeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
* 市场活动控制器
* */
public class ActivityController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if("/workbench/activity/getUserList.do".equals(path)){
            getUserList(request,response);
        }
        else if("/workbench/activity/save.do".equals(path)){
            saveActivity(request,response);
        }
        else if("/workbench/activity/pageList.do".equals(path)){
            pageList(request,response);
        }
        else if("/workbench/activity/delete.do".equals(path)){
            delete(request,response);
        }
        else if("/workbench/activity/getUserListAndActivity.do".equals(path)){
            getUserListAndActivity(request,response);
        }
        else if("/workbench/activity/modify.do".equals(path)){
            modify(request,response);
        }
        else if("/workbench/activity/detail.do".equals(path)){
            detail(request,response);
        }

    }



    //获取详细信息
    private void detail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        ActivityService  activityService = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());

        Activity activity = activityService.detail(id);
        request.setAttribute("activity",activity);
        request.getRequestDispatcher("/workbench/activity/detail.jsp").forward(request,response);
    }

    //修改
    private void modify(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String owner = request.getParameter("owner");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String description = request.getParameter("description");
        String cost = request.getParameter("cost");
        String editTime = DateTimeUtil.getSysTime();
        String editBy = ((User)request.getSession().getAttribute("user")).getName();
        Activity activity = new Activity();
        activity.setId(id);
        activity.setName(name);
        activity.setOwner(owner);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        activity.setCost(cost);
        activity.setDescription(description);
        //修改人
        activity.setEditBy(editBy);
        //修改时间
        activity.setEditTime(editTime);

        ActivityService  activityService = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        try {
            boolean flag = activityService.modify(activity);
            PrintJson.printJsonFlag(response,flag);
        } catch (ActivityModifyException e) {
            Map<String,Object> map = new HashMap<>();
            map.put("code",-1);
            map.put("msg",e.getMessage());
            PrintJson.printJsonObj(response,map);
        }
    }

    //获取用户列表及活动
    private void getUserListAndActivity(HttpServletRequest request, HttpServletResponse response) {
        String activityId = request.getParameter("id");
        ActivityService activityService = (ActivityService)ServiceFactory.getService(new ActivityServiceImpl());
        Map<String,Object> map = activityService.getUserListAndActivity(activityId);
        PrintJson.printJsonObj(response,map);
    }

    //删除
    private void delete(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids");
        System.out.println(ids);
        ActivityService activityService = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        try {
            boolean flag = activityService.delete(ids);
            PrintJson.printJsonFlag(response,flag);
        } catch (ActivityDeleteException | ActivityRemarkDeleteException e) {
            Map<String,Object> map = new HashMap<>();
            map.put("code",-1);
            map.put("msg",e.getMessage());
            PrintJson.printJsonObj(response,map);
        }
    }

    //分页查询
    private void pageList(HttpServletRequest request, HttpServletResponse response) {
        String pageNoStr = request.getParameter("pageNo");
        String pageSizeStr = request.getParameter("pageSize");
        String name = request.getParameter("name");
        String owner = request.getParameter("owner");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        int pageNo = Integer.valueOf(pageNoStr);
        int pageSize = Integer.valueOf(pageSizeStr);
        int pageStart = (pageNo-1)*pageSize;
        HashMap<String,Object> map = new HashMap<>();
        map.put("pageStart",pageStart);
        map.put("pageSize",pageSize);
        map.put("name",name);
        map.put("owner",owner);
        map.put("startDate",startDate);
        map.put("endDate",endDate);

        ActivityService activityService = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        PaginationVo<Activity> pageList = activityService.getPaginationVo(map);
        PrintJson.printJsonObj(response,pageList);
    }

    //添加活动
    private void saveActivity(HttpServletRequest request, HttpServletResponse response) {
        /*
            id
            owner;
            name;
            startDate;
            endDate;
            cost;
            description;
            createTime;
            createBy;
            editTime;
            editBy;
       */
        String id = UUIDUtil.getUUID();
        String owner = request.getParameter("owner");
        String name = request.getParameter("name");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String cost = request.getParameter("cost");
        String description = request.getParameter("description");
        String createTime = DateTimeUtil.getSysTime();
        String createBy = ((User)request.getSession().getAttribute("user")).getName();

        Activity activity = new Activity();
        activity.setId(id);
        activity.setOwner(owner);
        activity.setName(name);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        activity.setCost(cost);
        activity.setCreateTime(createTime);
        activity.setCreateBy(createBy);
        activity.setDescription(description);
        ActivityService activityService = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        try{
            boolean flag = activityService.save(activity);
            PrintJson.printJsonFlag(response,flag);
        }
        catch (ActivitySaveException e){
           String msg =  e.getMessage();
           Map<String,Object> map = new HashMap<>();
           map.put("code",-1);
           map.put("msg",msg);
           PrintJson.printJsonObj(response,map);
        }
    }

    private void getUserList(HttpServletRequest request, HttpServletResponse response) {
        UserService service = (UserService) ServiceFactory.getService(new UserServiceImpl());

        List<User> users= service.getUserList();
        PrintJson.printJsonObj(response,users);
    }


}