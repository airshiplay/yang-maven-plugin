//package com.airlenet.yang.aop;
//
//
// import com.airlenet.yang.common.PlayNetconfDevice;
// import com.tailf.jnc.NetconfSession;
// import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
///**
// * Created by Administrator on 16-7-1.
// */
//
//@Component
//@Aspect
//public class NetconfAspect {
//
//    private static Logger logger = LoggerFactory.getLogger(NetconfAspect.class);
//
//    //更新操作
//    @Pointcut("execution(* com.certus.vcpe.connector.invoke.impl.ConfigServiceImpl.*(..))")
//    public void configPoint() {
//
//    }
//
//    //查询操作
//    @Pointcut("execution(* com.certus.vcpe.connector.invoke.impl.QueryServiceImpl.*(..))")
//    public void queryPoint() {
//
//    }
//
//    @Around("configPoint()")
//    public Object flexOperateLogPoint(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//        Object[] inputs = joinPoint.getArgs();
//        PlayNetconfDevice flexNetconfDevice = (PlayNetconfDevice) inputs[0];
//        String engIP = flexNetconfDevice.getEngIP();
//        NetconfSession netconfSession = this.getEditNetconfSession(engIP);
//        flexNetconfDevice.setNetconfSession(netconfSession);
//        Object returnResult = null;
//        boolean openTransaction = flexNetconfDevice.isOpenTransaction();
//        if (openTransaction) {
//            try {
//                netconfSession.discardChanges();//现将 上次没有提交的配置 还原
//                netconfSession.lock(NetconfSession.CANDIDATE);
//                netconfSession.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
//                returnResult = joinPoint.proceed();
//                netconfSession.confirmedCommit(60);// candidates are now updated 1分钟内没有确认 则还原配置
//                netconfSession.commit();//now commit them 确认提交
//                this.returnEditSession(engIP, netconfSession);
//            } catch (IOException ioe) {
//                //获取netconf连接
//                this.updateNetconfSession(engIP);
//                logger.error("Method Name[" + methodName + "]: " + ioe);
//                throw ioe;
//            } catch (ReleaseSessionException e) {
//                this.releaseNetconfSession(engIP);
//                logger.error("Method Name[" + methodName + "]: " + e);
//            }  catch (Exception e) {
//                this.returnEditSession(engIP, netconfSession);
//                logger.error("Method Name[" + methodName + "]: " + e);
//                throw e;
//            } finally {
//                netconfSession.unlock(NetconfSession.CANDIDATE);
//            }
//        } else {
//            try {
//                returnResult = joinPoint.proceed();
//                this.returnEditSession(engIP, netconfSession);
//            } catch (IOException ioe) {
//                this.updateNetconfSession(engIP);
//                logger.error("Method Name[" + methodName + "]: " + ioe);
//                throw ioe;
//            } catch (ReleaseSessionException e) {
//                this.releaseNetconfSession(engIP);
//                logger.error("Method Name[" + methodName + "]: " + e);
//            }  catch (Exception e) {
//                this.returnEditSession(engIP, netconfSession);
//                logger.error("Method Name[" + methodName + "]: " + e);
//                throw e;
//            }
//        }
//        return returnResult;
//    }
//
//    @Around("queryPoint()")
//    public Object aroundCreate(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//        Object[] inputs = joinPoint.getArgs();
//        FlexNetconfDevice flexNetconfDevice = (FlexNetconfDevice) inputs[0];
//        String engIP = flexNetconfDevice.getEngIP();
//        NetconfSession netconfSession = this.getNetconfSession(engIP);
//        flexNetconfDevice.setNetconfSession(netconfSession);
//        Object returnResult = null;
//        try {
//            returnResult = joinPoint.proceed();
//            this.returnSession(engIP, netconfSession);
//        } catch (IOException ioe) {
//            this.updateNetconfSession(engIP);
//            logger.error("Method Name[" + methodName + "]: " + ioe);
//            throw ioe;
//        } catch (ReleaseSessionException e) {
//            this.releaseNetconfSession(engIP);
//            logger.error("Method Name[" + methodName + "]: " + e);
//        } catch (Exception e) {
//            this.returnSession(engIP, netconfSession);
//            logger.error("Method Name[" + methodName + "]: " + e);
//            throw e;
//        }
//        return returnResult;
//    }
//
//}
