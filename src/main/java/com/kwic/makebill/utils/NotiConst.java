package com.kwic.makebill.utils;

public class NotiConst {

    public static final String START_TYPE = "START_TYPE";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String FINANCE_BILL_INFO = "FINANCE_BILL_INFO";

    public enum START_TYPES {
        RUN_MLNI(0), RUN_CREDIT(0), RUN_INSURE(0), RUN_ALL(0),
        RUN_FINANCE_INIT(1), RUN_FINANCE_EACH(1), RUN_FINANCE_ALL(1), RUN_FINANCE_CARDBILL(1);


        public int getType() {
            return type;
        }

        private int type;

        START_TYPES(int type) {
            this.type=type;
        }
    }
}
