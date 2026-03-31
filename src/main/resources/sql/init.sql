-- drop existing tables

DROP TABLE IF EXISTS Staff CASCADE;
DROP TABLE IF EXISTS Payment CASCADE;
DROP TABLE IF EXISTS Booking CASCADE;
DROP TABLE IF EXISTS Customer CASCADE;
DROP TABLE IF EXISTS Room CASCADE;
DROP TABLE IF EXISTS HotelPhone CASCADE;
DROP TABLE IF EXISTS Hotel CASCADE;
DROP TABLE IF EXISTS Appuser CASCADE;

-- create tables

CREATE TABLE Appuser(
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
    dob DATE,
    username VARCHAR(15) UNIQUE NOT NULL check (username like 'admin%'),
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Hotel(
    hotel_id SERIAL PRIMARY KEY,
    hotel_name VARCHAR(100) NOT NULL,
    streetName VARCHAR(100) NOT NULL,
    landmark VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE HotelPhone(
    phone_id SERIAL PRIMARY KEY,
    hotel_id INT REFERENCES Hotel(hotel_id) ON DELETE CASCADE,
    phone_number VARCHAR(15) UNIQUE NOT NULL
);

CREATE TABLE Room(
    hotel_id INT,
    room_number  INT,
    room_type VARCHAR(30) NOT NULL,
    price_per_day NUMERIC(10,2) NOT NULL CHECK(price_per_day > 0),
    status varchar(20) DEFAULT 'available' CHECK (status IN ('available','booked','occupied','maintenance','decommissioned')),
    PRIMARY KEY(hotel_id,room_number),
    FOREIGN KEY(hotel_id) REFERENCES Hotel(hotel_id) ON DELETE CASCADE
);

CREATE TABLE Customer(
    aadhar_num CHAR(12) PRIMARY KEY CHECK(CHAR_LENGTH(aadhar_num) = 12),
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) CHECK(gender IN('Male','Female','Other')) NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100),
    state VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    street VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE Booking(
    booking_id SERIAL PRIMARY KEY,
    aadhar_num CHAR(12) NOT NULL,
    hotel_id INT NOT NULL,
    room_number INT NOT NULL,
    check_in DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'confirmed' CHECK(status IN ('confirmed','checked_in','checked_out','cancelled')),
    check_out DATE NOT NULL,
    FOREIGN KEY(aadhar_num) REFERENCES Customer(aadhar_num),
    FOREIGN KEY(hotel_id,room_number) REFERENCES Room(hotel_id,room_number),
    CHECK(check_in < check_out)
);

CREATE TABLE Payment(
    booking_id INT PRIMARY KEY,
    extra_services_cost NUMERIC(10,2),
    payment_date DATE,
    mode VARCHAR(15) CHECK(mode IN ('upi','card','cash','bank_transfer')),
    status VARCHAR(15) CHECK(status IN('pending','completed')),
    rating NUMERIC(2,1) CHECK(rating BETWEEN 0 AND 5),
    FOREIGN KEY(booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
);

CREATE TABLE Staff(
    hotel_id INT,
    staff_id INT,
    name VARCHAR(100),
    gender VARCHAR(10) CHECK(gender IN ('Male','Female','Other')),
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    join_date DATE NOT NULL,
    salary NUMERIC(10,2) NOT NULL CHECK(salary > 0),
    role VARCHAR(25) NOT NULL CHECK(role IN('Manager', 'Receptionist', 'Housekeeping', 'Chef', 'Waiter', 'Maintenance','Security')),
    is_active BOOLEAN DEFAULT TRUE,
    PRIMARY KEY(hotel_id,staff_id),
    FOREIGN KEY(hotel_id) REFERENCES Hotel(hotel_id) ON DELETE CASCADE
);

-- insert hotels data

INSERT INTO Hotel (hotel_name, streetName, landmark, is_active) VALUES
('Country Inn & Suites by Radisson', 'Rajathadri Road, Vidyaratna Nagar', 'D.C. Office', TRUE),
('Home Town Galleria', 'Main road, Manipal Commercial Complex', 'KMC', TRUE),
('Fortune Valley View', 'MAHE Campus, Madhav Nagar', 'MAHE Campus', TRUE),
('Tea Tree Suites', 'Ramani Pal Road, 2nd Cross', 'Vidyaratna Nagar', TRUE),
('Hotel Green Park Suites', 'Academy Towers, Tiger Circle', 'Opposite Corporation Bank', TRUE),
('Hotel Tranquil', 'Dr VS Acharya Rd', 'Vidyarathna Nagar', TRUE),
('Hotel Madhuvan Serai', 'Upendra Nagar', 'Smrithi Bhavan', TRUE),
('Hotel Sarah International', 'End Point Road, Vidyaratna Nagar', 'Mandavi Square', TRUE),
('Shoolin Resorts', 'Ananth Kalyan Nagar', 'Ananth Kalyan Nagar', TRUE),
('Foursquare Comforts', 'D.C. Office Road', 'Vidyaratna Nagar', TRUE),
('Hotel Ashlesh', 'Karkala Road', 'Opposite MIT', TRUE),
('RS Bhavan', 'Manipal Alevoor Road', 'Opposite BSNL Office', TRUE),
('Treebo Pratham Inn', 'Rudra Priya Nagar, Hayagreeva Nagar', 'Opposite Naturals Ice Cream, near Udupi Railway Station', TRUE),
('The High Point Inn & Suites', 'VK Odyssey, DC Office Road', 'Vidyaratna Nagar', TRUE),
('Hotel Sun Bright Residency', 'Manipal-Alevoor Road', 'near Laxmindra Nagar', TRUE);

INSERT INTO HotelPhone (phone_id, hotel_id, phone_number) VALUES
(18, 1, '+918884480902'), (17, 1, '+919148446432'), (1, 1, '+918202701600'), (19, 1, '+919148446434'),
(20, 2, '+918204200990'), (2, 2, '+918204200991'), (21, 2, '+917760141299'), 
(22, 3, '+916366224993'), (3, 3, '+918203500600'),
(4, 4, '+918204207777'), (23, 4, '+919845122077'),
(5, 5, '+918204295701'), (24, 5, '+918204295702'),
(25, 6, '+918202571111'), (6, 6, '+919980550888'),
(7, 7, '+917829901250'), (26, 7, '+918202571667'), (27, 7, '+918202571668'), (28, 7, '+917829901251'),
(29, 8, '+918202571618'), (8, 8, '+918202574666'), (30, 8, '+918202571666'), (31, 8, '+918277114223'),
(32, 9, '+917026280444'), (9, 9, '+918204202444'),
(10, 10, '+918867173111'), 
(38, 11, '+919739271272'),
(40, 12, '+918095252277'),
(13, 13, '+919322800100'),
(14, 14, '+918296665693'), (41, 14, '+919110618693'),
(16, 15, '+917760284379'), (15, 15, '+917760534379');

-- insert rooms data
-- =========================
DO $$
DECLARE i INT;
BEGIN

-- 1. Treebo Pratham Inn (hotel_id = 13)
FOR i IN 1..8 LOOP
  INSERT INTO Room VALUES (13,i,'Standard',1200,'available');
END LOOP;

FOR i IN 9..29 LOOP
  INSERT INTO Room VALUES (13,i,'Deluxe',1800,'available');
END LOOP;


-- 2. Country Inn & Suites (hotel_id = 1)
FOR i IN 1..28 LOOP
  INSERT INTO Room VALUES (1,i,'Superior',2500,'available');
END LOOP;

FOR i IN 29..48 LOOP
  INSERT INTO Room VALUES (1,i,'Deluxe',3500,'available');
END LOOP;

FOR i IN 49..58 LOOP
  INSERT INTO Room VALUES (1,i,'Suite',5000,'available');
END LOOP;


-- 3. Home Town Galleria (hotel_id = 2)
FOR i IN 1..12 LOOP
  INSERT INTO Room VALUES (2,i,'Standard',1000,'available');
END LOOP;

FOR i IN 13..20 LOOP
  INSERT INTO Room VALUES (2,i,'Deluxe',1500,'available');
END LOOP;


-- 4. Fortune Valley View (hotel_id = 3)
FOR i IN 1..29 LOOP
  INSERT INTO Room VALUES (3,i,'Deluxe',3000,'available');
END LOOP;

FOR i IN 30..47 LOOP
  INSERT INTO Room VALUES (3,i,'Premium',4000,'available');
END LOOP;

FOR i IN 48..59 LOOP
  INSERT INTO Room VALUES (3,i,'Fortune Club',5000,'available');
END LOOP;

FOR i IN 60..61 LOOP
  INSERT INTO Room VALUES (3,i,'Junior Suite',6500,'available');
END LOOP;

FOR i IN 62..64 LOOP
  INSERT INTO Room VALUES (3,i,'Executive Suite',8000,'available');
END LOOP;

FOR i IN 65..68 LOOP
  INSERT INTO Room VALUES (3,i,'Director Suite',10000,'available');
END LOOP;


-- 5. Tea Tree Suites (hotel_id = 4)
FOR i IN 1..16 LOOP
  INSERT INTO Room VALUES (4,i,'Studio',2000,'available');
END LOOP;

FOR i IN 17..24 LOOP
  INSERT INTO Room VALUES (4,i,'Suite',3500,'available');
END LOOP;


-- 6. Hotel Green Park Suites (hotel_id = 5)
FOR i IN 1..20 LOOP
  INSERT INTO Room VALUES (5,i,'Deluxe',1800,'available');
END LOOP;

FOR i IN 21..30 LOOP
  INSERT INTO Room VALUES (5,i,'Suite',3000,'available');
END LOOP;


-- 7. Hotel Tranquil (hotel_id = 6)
FOR i IN 1..12 LOOP
  INSERT INTO Room VALUES (6,i,'Standard',1200,'available');
END LOOP;

FOR i IN 13..22 LOOP
  INSERT INTO Room VALUES (6,i,'Deluxe',1800,'available');
END LOOP;


-- 8. Hotel Madhuvan Serai (hotel_id = 7)
FOR i IN 1..20 LOOP
  INSERT INTO Room VALUES (7,i,'Deluxe',2000,'available');
END LOOP;

FOR i IN 21..28 LOOP
  INSERT INTO Room VALUES (7,i,'Suite',3500,'available');
END LOOP;


-- 9. Hotel Sarah International (hotel_id = 8)
FOR i IN 1..15 LOOP
  INSERT INTO Room VALUES (8,i,'Standard',1200,'available');
END LOOP;

FOR i IN 16..27 LOOP
  INSERT INTO Room VALUES (8,i,'Deluxe',1800,'available');
END LOOP;

FOR i IN 28..35 LOOP
  INSERT INTO Room VALUES (8,i,'Executive',2500,'available');
END LOOP;


-- 10. Shoolin Resorts (hotel_id = 9)
FOR i IN 1..10 LOOP
  INSERT INTO Room VALUES (9,i,'Cottage',3000,'available');
END LOOP;

FOR i IN 11..18 LOOP
  INSERT INTO Room VALUES (9,i,'Deluxe',2200,'available');
END LOOP;


-- 11. Foursquare Comforts (hotel_id = 10)
FOR i IN 1..14 LOOP
  INSERT INTO Room VALUES (10,i,'Standard',1200,'available');
END LOOP;

FOR i IN 15..26 LOOP
  INSERT INTO Room VALUES (10,i,'Deluxe',1700,'available');
END LOOP;


-- 12. Hotel Ashlesh (hotel_id = 11)
FOR i IN 1..15 LOOP
  INSERT INTO Room VALUES (11,i,'Standard Non-AC',900,'available');
END LOOP;

FOR i IN 16..30 LOOP
  INSERT INTO Room VALUES (11,i,'Standard AC',1200,'available');
END LOOP;

FOR i IN 31..40 LOOP
  INSERT INTO Room VALUES (11,i,'Deluxe',1600,'available');
END LOOP;


-- 13. RS Bhavan (hotel_id = 12)
FOR i IN 1..15 LOOP
  INSERT INTO Room VALUES (12,i,'Standard',800,'available');
END LOOP;


-- 14. The High Point Inn & Suites (hotel_id = 14)
FOR i IN 1..22 LOOP
  INSERT INTO Room VALUES (14,i,'Deluxe',2000,'available');
END LOOP;

FOR i IN 23..32 LOOP
  INSERT INTO Room VALUES (14,i,'Suite',3500,'available');
END LOOP;


-- 15. Hotel Sun Bright Residency (hotel_id = 15)
FOR i IN 1..13 LOOP
  INSERT INTO Room VALUES (15,i,'Standard',1100,'available');
END LOOP;

FOR i IN 14..25 LOOP
  INSERT INTO Room VALUES (15,i,'Deluxe',1600,'available');
END LOOP;

END $$;

-- ============
-- triggers

-- 1. cascading decommission of rooms and staff
CREATE OR REPLACE FUNCTION cascade_hotel_soft_delete()
RETURNS TRIGGER AS $$
BEGIN
    -- Decommission all rooms of this hotel
    UPDATE Room SET status = 'decommissioned' WHERE hotel_id = NEW.hotel_id;
    
    -- Deactivate all staff of this hotel
    UPDATE Staff SET is_active = FALSE WHERE hotel_id = NEW.hotel_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_hotel_soft_delete
AFTER UPDATE OF is_active ON Hotel
FOR EACH ROW
WHEN (OLD.is_active = TRUE AND NEW.is_active = FALSE)
EXECUTE FUNCTION cascade_hotel_soft_delete();

-- 2. restoring all rooms and staff
CREATE OR REPLACE FUNCTION cascade_hotel_reactivate()
RETURNS TRIGGER AS $$
BEGIN
    -- Restore rooms to available
    UPDATE Room SET status = 'available' WHERE hotel_id = NEW.hotel_id;
    
    -- Reactivate all staff
    UPDATE Staff SET is_active = TRUE WHERE hotel_id = NEW.hotel_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_hotel_reactivate
AFTER UPDATE OF is_active ON Hotel
FOR EACH ROW
WHEN (OLD.is_active = FALSE AND NEW.is_active = TRUE)
EXECUTE FUNCTION cascade_hotel_reactivate();

-- 3. automatically setting the room status based on booking status)
CREATE OR REPLACE FUNCTION room_status_auto()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        IF (NEW.status = 'confirmed') THEN
            UPDATE Room SET status = 'booked'
            WHERE hotel_id = NEW.hotel_id AND room_number
            = NEW.room_number;
        ELSIF (NEW.status = 'checked_in') THEN
            UPDATE Room SET status = 'occupied'
            WHERE hotel_id = NEW.hotel_id AND room_number =
            NEW.room_number;
        END IF;

    ELSIF (TG_OP = 'UPDATE') THEN
        IF (NEW.status = 'confirmed') THEN
            UPDATE Room SET status = 'booked'
            WHERE hotel_id = NEW.hotel_id AND room_number =
            NEW.room_number;
        ELSIF (NEW.status = 'checked_in') THEN
            UPDATE Room SET status = 'occupied'
            WHERE hotel_id = NEW.hotel_id AND room_number =
            NEW.room_number;
        ELSIF (NEW.status = 'checked_out' OR NEW.status = 'cancelled')
        THEN
            UPDATE Room SET status = 'available'
            WHERE hotel_id = NEW.hotel_id AND room_number =
            NEW.room_number;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_room_status
AFTER INSERT OR UPDATE ON Booking
FOR EACH ROW
EXECUTE FUNCTION room_status_auto();

-- 4. automatic insertion of corresponding record when a booking is made
CREATE OR REPLACE FUNCTION auto_insert_payment()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Payment(booking_id, extra_services_cost, payment_date, mode, status, rating)
    VALUES (NEW.booking_id, 0, NEW.check_out, NULL, 'pending', NULL);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auto_insert_payment
AFTER INSERT ON Booking
FOR EACH ROW
EXECUTE FUNCTION auto_insert_payment();

-- 5. Sync payment date upon updation in booking table
CREATE OR REPLACE FUNCTION sync_payment_date()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.check_out <> NEW.check_out THEN
        UPDATE Payment SET payment_date = NEW.check_out
        WHERE booking_id = NEW.booking_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_payment_date
AFTER UPDATE OF check_out ON Booking
FOR EACH ROW
EXECUTE FUNCTION sync_payment_date();

-- 6. password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE OR REPLACE FUNCTION hash_password()
RETURNS TRIGGER AS $$
BEGIN
    NEW.password = crypt(NEW.password, gen_salt('bf'));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hash_password
BEFORE INSERT ON Appuser
FOR EACH ROW
EXECUTE FUNCTION hash_password();

-- 7. double booking check
CREATE OR REPLACE FUNCTION check_double_booking()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM Booking
        WHERE hotel_id = NEW.hotel_id
        AND room_number = NEW.room_number
        AND status <> 'checked_out'
        AND booking_id <> NEW.booking_id
        AND (NEW.check_in < check_out AND NEW.check_out > check_in)
    ) THEN
        RAISE EXCEPTION 'Room already booked for these dates.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_double_booking
BEFORE INSERT OR UPDATE ON Booking
FOR EACH ROW
EXECUTE FUNCTION check_double_booking();

--