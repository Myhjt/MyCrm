package com.hjt.MyCRM.workbench.service.impl;

import com.hjt.MyCRM.exception.*;
import com.hjt.MyCRM.utils.DateTimeUtil;
import com.hjt.MyCRM.utils.SqlSessionUtil;
import com.hjt.MyCRM.utils.UUIDUtil;
import com.hjt.MyCRM.vo.PaginationVo;
import com.hjt.MyCRM.workbench.dao.*;
import com.hjt.MyCRM.workbench.domain.*;
import com.hjt.MyCRM.workbench.service.ClueService;

import java.util.List;
import java.util.Map;

public class ClueServiceImpl implements ClueService {


    private ClueDao clueDao = (ClueDao) SqlSessionUtil.getSqlSession().getMapper(ClueDao.class);
    private ClueActivityRelationDao clueActivityRelationDao = (ClueActivityRelationDao) SqlSessionUtil.getSqlSession().getMapper(ClueActivityRelationDao.class);
    private ClueRemarkDao clueRemarkDao = (ClueRemarkDao) SqlSessionUtil.getSqlSession().getMapper(ClueRemarkDao.class);

    private CustomerDao customerDao = (CustomerDao) SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);
    private CustomerRemarkDao customerRemarkDao = (CustomerRemarkDao) SqlSessionUtil.getSqlSession().getMapper(CustomerRemarkDao.class);


    private ContactsDao contactsDao = (ContactsDao) SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
    private ContactsRemarkDao contactsRemarkDao = (ContactsRemarkDao) SqlSessionUtil.getSqlSession().getMapper(ContactsRemarkDao.class);
    private ContactsActivityRelationDao contactsActivityRelationDao = (ContactsActivityRelationDao) SqlSessionUtil.getSqlSession().getMapper(ContactsActivityRelationDao.class);

    private ActivityDao activityDao = (ActivityDao) SqlSessionUtil.getSqlSession().getMapper(ActivityDao.class);
    private TranDao tranDao = (TranDao) SqlSessionUtil.getSqlSession().getMapper(TranDao.class);
    private TranHistoryDao tranHistoryDao = (TranHistoryDao) SqlSessionUtil.getSqlSession().getMapper(TranHistoryDao.class);
    @Override
    public List<Activity> getActivityListByActivityName(String activityName) {
        return activityDao.getActivityListByActivityName(activityName);
    }

    @Override
    public Clue getByIdForModify(String clueId) {
        return clueDao.getByIdForModify(clueId);
    }

    @Override
    public boolean modify(Clue clue) throws ClueModifyException {
        int result = clueDao.modify(clue);
        if(result!=1){
            throw new ClueModifyException("??????????????????");
        }
        return true;
    }

    @Override
    public List<Activity> getActivityListByClueId(String clueId) {
        return activityDao.getActivityListByClueId(clueId);
    }

    @Override
    public List<Activity> getActivityListByActivityNameNotClueId(String activityName, String clueId) {
        return activityDao.getActivityListByActivityNameAndClueId(activityName,clueId);
    }

    @Override
    public boolean unbound(String relationId) throws ClueActivityRelationUnboundException {
        int result = clueActivityRelationDao.unbound(relationId);
        if(result!=1){
            throw new ClueActivityRelationUnboundException("??????????????????");
        }
        return true;
    }

    @Override
    public boolean bound(List<Map<String, String>> datas) throws ClueActivityRelationBoundException {
        int result = clueActivityRelationDao.bound(datas);
        if(result!=datas.size()){
            throw new ClueActivityRelationBoundException("????????????????????????");
        }
        return true;
    }

    @Override
    public boolean save(Clue clue) throws ClueSaveException {
        int nums = clueDao.save(clue);
        if(nums!=1){
            throw new ClueSaveException("??????????????????");
        }
        return true;
    }

    @Override
    public PaginationVo<Clue> getPagination(Map<String, Object> map) {
        PaginationVo<Clue> cluePaginationVo = new PaginationVo<>();

        Integer total = clueDao.getTotal(map);
        List<Clue> clueList = clueDao.getPageList(map);
        cluePaginationVo.setTotal(total);
        cluePaginationVo.setDataList(clueList);
        return cluePaginationVo;
    }

    @Override
    public Clue getByIdForDetail(String clueId) {
        return clueDao.getByIdForDetail(clueId);
    }

    @Override
    public boolean convert(String clueId, Tran t, String activityId) throws ClueConvertException {
        String createTime = DateTimeUtil.getSysTime();
        String createBy = t.getCreateBy();
        //1???????????????id??????????????????
        Clue clue = clueDao.getById(clueId);

        //2??????????????????????????????????????????????????????????????????????????????
        String companyName = clue.getCompany();
        Customer customer = customerDao.getByName(companyName);
        //???????????????????????????????????????????????????
        if(customer==null){
            customer = new Customer();
            customer.setId(UUIDUtil.getUUID());
            customer.setName(companyName);
            customer.setPhone(clue.getMphone());
            customer.setWebsite(clue.getWebsite());
            customer.setCreateTime(createTime);
            customer.setAddress(clue.getAddress());
            customer.setNextContactTime(t.getNextContactTime());
            customer.setDescription(clue.getDescription());
            customer.setContactSummary(t.getContactSummary());
            customer.setCreateBy(createBy);
            //????????????
            int result  = customerDao.save(customer);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
        }

        //3????????????????????????????????????????????????????????????
        Contacts contacts = new Contacts();
        contacts.setId(UUIDUtil.getUUID());
        contacts.setCreateBy(createBy);
        contacts.setCreateTime(createTime);
        contacts.setContactSummary(clue.getContactSummary());
        contacts.setAddress(clue.getAddress());
        contacts.setAppellation(clue.getAppellation());
        contacts.setDescription(clue.getDescription());
        contacts.setMphone(clue.getMphone());
        contacts.setFullname(clue.getFullname());
        contacts.setCustomerId(customer.getId());
        contacts.setSource(clue.getSource());
        contacts.setJob(clue.getJob());
        contacts.setNextContactTime(clue.getNextContactTime());
        contacts.setEmail(clue.getEmail());
        int result = contactsDao.save(contacts);
        if(result!=1){
            throw new ClueConvertException("??????????????????");
        }

        //4?????????????????????????????????????????????????????????????????????
        //????????????????????????????????????
        List<ClueRemark> clueRemarkList = clueRemarkDao.getByClueId(clue.getId());
        for(ClueRemark remark:clueRemarkList){
            //??????????????????
            CustomerRemark customerRemark = new CustomerRemark();
            customerRemark.setId(UUIDUtil.getUUID());
            customerRemark.setCreateBy(createBy);
            customerRemark.setCreateTime(createTime);
            customerRemark.setNoteContent(remark.getNoteContent());
            customerRemark.setCustomerId(customer.getId());
            customerRemark.setEditFlag("0");
            result = customerRemarkDao.save(customerRemark);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
            
            //?????????????????????
            ContactsRemark contactsRemark = new ContactsRemark();
            contactsRemark.setId(UUIDUtil.getUUID());
            contactsRemark.setCreateBy(createBy);
            contactsRemark.setCreateTime(createTime);
            contactsRemark.setNoteContent(remark.getNoteContent());
            contactsRemark.setContactsId(contacts.getId());
            contactsRemark.setEditFlag("0");
            result = contactsRemarkDao.save(contactsRemark);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
        }

        //5???????????????-??????????????????????????????-??????????????????
        List<ClueActivityRelation> clueActivityRelationList = clueActivityRelationDao.getListByClueId(clue.getId());
        for(ClueActivityRelation relation:clueActivityRelationList){
            ContactsActivityRelation carealtion = new ContactsActivityRelation();
            carealtion.setId(UUIDUtil.getUUID());
            carealtion.setActivityId(relation.getActivityId());
            carealtion.setContactsId(contacts.getId());
            result = contactsActivityRelationDao.save(carealtion);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
        }

        //6???????????????????????????????????????????????????
        if(t!=null){
            /*
            *   ??????????????????
            *   id,clueId,tranMoney,tranName,tranExpectedClosingDate,tranStage,tranActivityId,createTime,createBy
            * */
            t.setSource(clue.getSource());
            t.setOwner(clue.getOwner());
            t.setDescription(clue.getDescription());
            t.setCustomerId(customer.getId());
            t.setContactSummary(contacts.getDescription());
            t.setContactsId(contacts.getId());
            t.setNextContactTime(clue.getNextContactTime());
            //????????????
            result = tranDao.save(t);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
            //7?????????????????????????????????????????????????????????????????????
            TranHistory tranHistory = new TranHistory();
            tranHistory.setId(UUIDUtil.getUUID());
            tranHistory.setCreateBy(createBy);
            tranHistory.setCreateTime(createTime);
            tranHistory.setMoney(t.getMoney());
            tranHistory.setStage(t.getStage());
            tranHistory.setTranId(t.getId());
            tranHistory.setExpectedDate(t.getExpectedDate());
            result = tranHistoryDao.save(tranHistory);
            if(result!=1){
                throw new ClueConvertException("??????????????????");
            }
        }
        //8????????????????????????
        int sum = 0;
        List<ClueRemark> temp = clueRemarkDao.getByClueId(clueId);
        if(temp!=null){
            sum = temp.size();
        }
        result = clueRemarkDao.deleteByClueId(clueId);
        if(result!=sum){
            throw new ClueConvertException("??????????????????");
        }

        //9???????????????-????????????
        sum = clueActivityRelationList!=null?clueActivityRelationList.size():0;
        result = clueActivityRelationDao.deleteByClueId(clueId);
        if(result!=sum){
            throw new ClueConvertException("??????????????????");
        }

        //10???????????????
        result = clueDao.deleteById(clue.getId());
        if(result!=1){
            throw new ClueConvertException("??????????????????");
        }
        return true;
    }
}
