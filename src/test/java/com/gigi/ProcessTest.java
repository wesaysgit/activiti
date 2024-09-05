package com.gigi;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = GigiApatowApplication.class)
@RunWith(SpringRunner.class)
public class ProcessTest {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;

    @Test
    void deployProcessDefinition() throws FileNotFoundException {
        String path = "src/main/resources/activiti/gigi.bpmn20.xml";
        Deployment deploy = repositoryService.createDeployment()
                .addInputStream(path, new FileInputStream(path)).deploy();
        System.out.println("部署流程定义成功："+ deploy);
    }

    @Test
    void startProcessInstance() {
        //启动流程时传递的参数列表 这里根据实际情况 也可以选择不传
        Map<String, Object> variables = new HashMap<>();
        variables.put("姓名", "张三");
        variables.put("请假天数", 4);
        variables.put("请假原因", "我很累！");

        // 根据流程定义ID查询流程定义  leave:1:10004是我们刚才部署的流程定义的id
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId("gigi:2:10004")
                .singleResult();

        // 获取流程定义的Key
        String processDefinitionKey = processDefinition.getKey();

        //定义businessKey  businessKey一般为流程实例key与实际业务数据的结合
        //假设一个请假的业务 在数据库中的id是1001
        String businessKey = processDefinitionKey + ":" + "1001";
        //设置启动流程的人
        Authentication.setAuthenticatedUserId("xxyin");
        ProcessInstance processInstance = this.runtimeService
                .startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        System.out.println("流程启动成功：" + processInstance);
    }

}
