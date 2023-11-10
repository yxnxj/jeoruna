package com.prography1.eruna.util;

import org.springframework.batch.core.JobExecutionListener;


//@Component
//@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

//    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
//    private final Scheduler scheduler;
//    private final GroupRepository groupRepository;
//    private final AlarmService alarmService;
//    @Override
////    @Transactional
//    public void afterJob(JobExecution jobExecution) {
//        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//            try {
//                List<Alarm> alarms = (List<Alarm>) scheduler.getContext().get("alarms");
//
//                if(alarms == null) alarms = new ArrayList<>();
//                for(int i = 0 ; i < alarms.size(); i++){
//                    Alarm alarm = alarms.get(i);
//
//                    Groups group = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));
//                    alarmService.createAlarmScheduleInGroup(alarm, group);
//                }
//            } catch (SchedulerException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

}
