package org.daisleyharrison.security.common.utilities;

import org.daisleyharrison.security.common.models.Stage;

public class StageImpl implements Stage {
    public interface CloseAction {
        public void close();
    }
    
    
    private CloseAction action;

    public StageImpl(CloseAction action) {
        this.action = action;
    }

    @Override
    public void close() throws Exception {
        this.action.close();
    }
}
