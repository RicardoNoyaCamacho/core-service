package com.finsync.core.model;

public enum EmailReminderType {
    REMINDER_5_DAYS(5, "Recordatorio de pago"),
    REMINDER_3_DAYS(3, "Tu pago se acerca"),
    REMINDER_LAST_DAY(0, "¡Último día para pagar!");

    private final int daysBeforeDue;
    private final String subject;

    EmailReminderType(int daysBeforeDue, String subject) {
        this.daysBeforeDue = daysBeforeDue;
        this.subject = subject;
    }

    public int getDaysBeforeDue() {
        return daysBeforeDue;
    }

    public String getSubject() {
        return subject;
    }
}
