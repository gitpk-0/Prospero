**Problem Summary:**
The date picker in the transaction form was automatically populating with the next day instead of the current day when adding a new transaction passed 9pm at night. This behavior was inconsistent with the calendar, which correctly highlighted the current day.

**Resolution Steps:**
To resolve this issue, the following steps were taken:

1. Identified that the issue was related to the default time zone used by the application.
2. Configured the default time zone by setting the `vaadin.timeZone` attribute in the Vaadin session to the user's local time zone.
3. Added the `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` configuration property to the application's configuration file to ensure consistent handling of time zones in database operations.
4. Verified that the date picker now selects the current date based on the user's time zone, resolving the issue.

These steps ensured that the date picker correctly reflects the user's local time zone and selects the current date by default.
