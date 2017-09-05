package com.airlenet.yang.netconf.aop;


import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.SessionClosedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by airshiplay on 16-7-1.
 */

@Component
@Aspect
public class NetconfAspect {

    private static Logger logger = LoggerFactory.getLogger(NetconfAspect.class);

    //更新操作
    @Pointcut("execution(* com.airlenet.yang.netconf.service.*.*(..))")
    public void configPoint() {

    }

    @Around("configPoint()")
    public Object flexOperateLogPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] inputs = joinPoint.getArgs();
        PlayNetconfDevice flexNetconfDevice = (PlayNetconfDevice) inputs[0];
        PlayNetconfSession playNetconfSession = flexNetconfDevice.getDefaultNetconfSession();

        flexNetconfDevice.setOpenTransaction(true);
        Object result = null;
        if (playNetconfSession.isCandidate()) {
            try {
                playNetconfSession.getNetconfSession().discardChanges();//现将 上次没有提交的配置 还原
                playNetconfSession.getNetconfSession().lock(NetconfSession.CANDIDATE);
                playNetconfSession.getNetconfSession().copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
                result = joinPoint.proceed();
                if (playNetconfSession.isConfirmedCommit()) {
                    playNetconfSession.getNetconfSession().confirmedCommit(60);// candidates are now updated 1分钟内没有确认 则还原配置
                }
                playNetconfSession.getNetconfSession().commit();//now commit them 确认提交
            } catch (SessionClosedException sce) {
                flexNetconfDevice.closeDefaultNetconfSession();
                logger.error("aop:" + methodName);
                throw sce;
            } catch (IOException ioe) {
                logger.error("aop:" + methodName);
                throw ioe;
            } catch (Exception ioe) {
                logger.error("aop:" + methodName);
                throw ioe;
            } finally {
                playNetconfSession.getNetconfSession().unlock(NetconfSession.CANDIDATE);
            }
        } else {
            result = joinPoint.proceed();
        }
        return result;
    }


}
