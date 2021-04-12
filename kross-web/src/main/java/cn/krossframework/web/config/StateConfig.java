package cn.krossframework.web.config;

import cn.krossframework.state.*;
import cn.krossframework.web.cat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class StateConfig {

    private static final Logger log = LoggerFactory.getLogger(StateConfig.class);


    @Bean
    StateGroupPool stateGroupPool() throws Exception {
        DefaultLazyTime defaultLazyTime = new DefaultLazyTime(100);
        CatFactory catFactory = new CatFactory(defaultLazyTime, new StateGroupConfig() {
            @Override
            public boolean autoUpdate() {
                return false;
            }

            @Override
            public String getStartState() {
                return Walk.CODE;
            }

            @Override
            public Collection<State> createStates() {
                List<State> stateList = new ArrayList<>(4);
                stateList.add(new Sleep());
                stateList.add(new Eat());
                stateList.add(new Walk());
                stateList.add(new DefaultErrorState());
                return stateList;
            }
        }) {
            @Override
            public StateGroup create(long id) {
                return new Cat(id, super.time, super.stateGroupConfig);
            }
        };
        CatPool catPool = new CatPool(catFactory);
        catPool.setRemoveDeposedStateGroupPeriod(2000);
        catPool.afterPropertiesSet();
        return catPool;
    }

    @Bean
    WorkerManager workerManager(StateGroupPool stateGroupPool) throws Exception {
        return new AbstractWorkerManager(50, 20, 100,
                200, 200, 20, stateGroupPool) {
        };
    }
}
