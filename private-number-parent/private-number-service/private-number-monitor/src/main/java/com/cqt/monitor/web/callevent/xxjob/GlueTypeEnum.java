package com.cqt.monitor.web.callevent.xxjob;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xuxueli
 * @date 17/4/26
 */
@Getter
@AllArgsConstructor
public enum GlueTypeEnum {

    /**
     * BEAN
     */
    BEAN("BEAN", false, null, null),

    /**
     * GLUE(Java)
     */
    GLUE_GROOVY("GLUE(Java)", false, null, null),

    /**
     * GLUE(Shell)
     */
    GLUE_SHELL("GLUE(Shell)", true, "bash", ".sh"),

    /**
     * GLUE(Python)
     */
    GLUE_PYTHON("GLUE(Python)", true, "python", ".py"),

    /**
     * GLUE(PHP)
     */
    GLUE_PHP("GLUE(PHP)", true, "php", ".php"),

    /**
     * GLUE(Nodejs)
     */
    GLUE_NODEJS("GLUE(Nodejs)", true, "node", ".js"),

    /**
     * GLUE(PowerShell)
     */
    GLUE_POWERSHELL("GLUE(PowerShell)", true, "powershell", ".ps1");

    private final String desc;
    private final boolean isScript;
    private final String cmd;
    private final String suffix;

    public static GlueTypeEnum match(String name) {
        for (GlueTypeEnum item : GlueTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return null;
    }

}
