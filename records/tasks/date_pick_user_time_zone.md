**Problem Summary:**
The date picker in the transaction form was automatically populating with the next day
instead of the current day when adding a new transaction passed 8pm (EST) at night.
This behavior was inconsistent with the calendar, which correctly highlighted the current
day.

**Goal:**
The goal was to implement client-side time zone checks in the
application running on the Heroku server. The objective was to ensure that calendar dates
are populated with the user's local date.

**Resolution Steps:**
To resolve this issue, the following steps were taken:

1. Retrieve the user's time zone using JavaScript's `Intl.DateTimeFormat()
   .resolvedOptions().timeZone`.

2. The `executeJs()` method was called within the `addTransactionBtn` click listener. The JavaScript code
   returns the user's time zone using `return userTimeZone`;.

3. The `then()` method was used to handle the result of the JavaScript execution. The result was obtained as a String
   using the `asString()` method.

4. To handle the case where the user's time zone is null or empty, a conditional check was added. If `userTimeZone` is
   null or empty, it was defaulted to "America/New_York".

5. Finally, the `userTimeZone` value was passed to the `addTransaction()` method, where the default date was adjusted based
   on the user's time zone using ZonedDateTime and LocalDate.

By incorporating these steps, the user time zone error was resolved, and the application successfully populates
the calendar dates with the user's local date.

**The following resolution attempt was not successful**

1. Identified that the issue was related to the default time zone used by the application.
2. Added the `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` configuration property to the application's
   configuration file to ensure consistent handling of time zones in database operations.
3. In the `main` method, the default time zone for the Java runtime is updated to match the system's default time zone
   by calling `TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()))`. This step ensures that all date and
   time calculations within the application are performed accurately, taking into account the correct time zone.
4. In the `addTransaction` method of the `TransactionView` class, the new transaction's date value is set to the current
   date by calling `newTransaction.setDate(Date.valueOf(ZonedDateTime.now().toLocalDate()));`.
5. Verified that the date picker now selects the current date based on the user's time zone, resolving the issue.

**Problem:** Retrieves the time zone from the server-side, not the client-side. It sets
the default time zone for the Java runtime based on the server's time zone. However,
it doesn't directly consider the time zone of the client accessing the application.
