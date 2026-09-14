# Rentora — Smart Vehicle Rental Marketplace

**Tagline:** *"Rent Vehicles. Earn Money. Drive Anywhere."*

A two-sided vehicle rental marketplace built with **Java (Servlets + JSP)**, **MVC + DAO architecture**, and **MySQL**, styled with a dark glassmorphism theme (Bootstrap 5, GSAP, AOS, Chart.js).

This package contains the **functional core** of the system — Authentication, Vehicle Listing, Search, Booking, and all three role dashboards (Renter / Owner / Admin) — fully wired end-to-end so it runs, builds, and demonstrates every required design pattern. It is meant as the foundation you build the remaining modules (payments/invoicing UI, reviews, wishlist, maintenance, reports, notifications UI) on top of, following the same layered pattern.

---

## 1. Prerequisites

- JDK 17+
- Apache Maven 3.9+
- Apache Tomcat 10.1+ (Jakarta EE 10 / `jakarta.*` namespace — **not** Tomcat 9)
- MySQL 8+

## 2. Database Setup

```bash
mysql -u root -p < database/schema.sql
```

This creates the `rentora_db` database, all tables, seed roles/categories, and a placeholder admin account. **Before going live, replace the placeholder `password_hash` for the admin row** — generate a real BCrypt hash (e.g. via the `PasswordHasher` utility class or an online BCrypt generator) and update it with:

```sql
UPDATE users SET password_hash = '<your-bcrypt-hash>' WHERE email = 'admin@rentora.com';
```

## 3. Configure Database Connection

`DBConnectionManager` reads connection details from system properties (with sensible localhost defaults). Pass them as JVM args in Tomcat's `setenv.sh` / `setenv.bat`, or edit the defaults directly in
`src/main/java/com/rentora/util/DBConnectionManager.java`:

```
-Drentora.db.url=jdbc:mysql://localhost:3306/rentora_db?useSSL=false&serverTimezone=UTC
-Drentora.db.user=root
-Drentora.db.password=yourpassword
```

## 4. Build & Deploy

```bash
mvn clean package
```

Deploy the generated `target/rentora.war` to Tomcat's `webapps/` folder (or via Tomcat Manager), then visit:

```
http://localhost:8080/rentora/
```

## 5. What's Implemented in This Package

| Layer | Included |
|---|---|
| **Database** | Full normalized schema (19 tables) — `database/schema.sql` |
| **Models** | User, OwnerProfile, Vehicle, Booking, Payment, Review, Notification |
| **DAO** | UserDAO, VehicleDAO, BookingDAO, NotificationDAO (interfaces + JDBC impls, PreparedStatements throughout) |
| **Design Patterns** | Singleton (`DBConnectionManager`), Factory (`VehicleFactory`), Strategy (`PaymentStrategy` + Card/Wallet/Coupon), Observer (`NotificationSubject` + in-app/email observers), DAO |
| **Services** | AuthService, VehicleService, BookingService (business rules: password strength, NIC/vehicle-number format, booking date-conflict prevention, fare calculation) |
| **Security** | BCrypt password hashing, `AuthFilter` (session gate), `RoleFilter` (RBAC per URL prefix), `EncodingFilter`, prepared statements everywhere (no string-concatenated SQL) |
| **Controllers (Servlets)** | Register/Login/Logout, vehicle search + details, renter booking, owner vehicle CRUD + booking accept/reject, admin vehicle/owner approval + dashboard |
| **Views (JSP)** | Landing page (hero + glass cards), login/register, vehicle browse + details + booking form, renter/owner/admin dashboards, shared navbar/footer/head fragments, 403/404/500 error pages |
| **Frontend** | Dark glassmorphism theme (`assets/css/style.css`), animated background orbs, GSAP entrance animations, AOS scroll reveals, skeleton-loading CSS, loading screen, live booking-price estimate JS |

## 6. What to Build Next (Not Yet Implemented)

These modules follow the exact same MVC/DAO/Service pattern already established — copy the shape of `VehicleService`/`BookingService` + their servlets/JSPs:

1. **Payments & Invoices** — `PaymentService` + `itext7`-based PDF invoice generation (dependency already in `pom.xml`)
2. **Reviews & Ratings** — `ReviewDAO`/`ReviewService`, submitted after a `COMPLETED` booking
3. **Wishlist** — simple join-table CRUD, already modeled in the schema
4. **Maintenance & Damage Reports** — owner-facing CRUD against the `maintenance` / `vehicle_damage_reports` tables
5. **Notifications UI** — a bell-icon dropdown pulling from `NotificationDAO.findByUser()` (backend already wired via the Observer pattern)
6. **Admin Analytics Dashboard** — Chart.js (already loaded in `footer.jsp`) fed by aggregate SQL queries (revenue by month, bookings by category, etc.)
7. **Coupons** — `CouponAdjustedStrategy` already exists in the Strategy layer; needs a `CouponDAO` + admin CRUD UI
8. **QR Code Booking Verification** — `zxing` dependency already included in `pom.xml`
9. **Export PDF / Excel Reports** — `itext7` for PDF; add Apache POI for Excel

## 7. Project Structure

See the architecture document delivered earlier for the full folder layout, sprint plan, and UML diagram list. The structure on disk in this package mirrors it exactly.



Register new Renter/Owner accounts via `/register`. Owner accounts require admin approval (status `PENDING` → `ACTIVE`) before they can log in.
