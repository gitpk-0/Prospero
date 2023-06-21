**Problem Summary:**
The date picker in the transaction form was automatically populating with the next day instead of the current day when adding a new transaction passed 9pm at night. This behavior was inconsistent with the calendar, which correctly highlighted the current day.

**Resolution Steps:**
To resolve this issue, the following steps were taken:

1. Identified that the issue was related to the default time zone used by the application.
2. Added the `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` configuration property to the application's configuration file to ensure consistent handling of time zones in database operations.
3. In the `main` method, the default time zone for the Java runtime is updated to match the system's default time zone by calling `TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()))`. This step ensures that all date and time calculations within the application are performed accurately, taking into account the correct time zone.
4. In the `addTransaction` method of the `TransactionView` class, the new transaction's date value is set to the current date by calling `newTransaction.setDate(Date.valueOf(ZonedDateTime.now().toLocalDate()));`.
5. Verified that the date picker now selects the current date based on the user's time zone, resolving the issue.

These steps ensured that the date picker correctly reflects the user's local time zone and selects the current date by default.
