use inha_item_borrow_service;


CREATE TABLE admin_role(
    id int auto_increment,
    role varchar(15)
);

INSERT INTO admin_role(role) VALUE("PRESIDENT");
INSERT INTO admin_role(role) VALUE("VICE_PRESIDENT");
INSERT INTO admin_role(role) VALUE("DIVISION_HEADER");
INSERT INTO admin_role(role) VALUE("DIVISION_MEMBER");

-- admin table 생성
CREATE TABLE admin(
    id varchar(20) NOT NULL primary key,
    password varchar(60) NOT NULL,
    email varchar(50) NOT NULL,
    name varchar(10) NOT NULL,
    phonenumber char(13) NOT NULL,
    position varchar(15) NOT NULL,
    foreign key position references admin_role(role)
);

-- borrower table 생성
CREATE TABLE borrower(
    id varchar(50) NOT NULL primary key,
    password varchar(60) NOT NULL,
    email varchar(50) NOT NULL,
    name varchar(10) NOT NULL,
    phonenumber char(13) NOT NULL,
    student_number varchar(50) NOT NULL,
    account_number varchar(20) NOT NULL,
    withdrawal boolean default false,
    ban boolean default false
);

create table item (
    id int NOT NULL primary key auto_increment,
    name varchar(10) NOT NULL,
    location varchar(10) NOT NULL,
    password varchar(8) NOT NULL,
    delete_reason varchar(50),
    price int NOT NULL,
    state varchar(9) NOT NULL default 'AFFORD'
);

create table signup_request(
    id varchar(50) NOT NULL primary key,
    password varchar(60) NOT NULL,
    email varchar(50) NOT NULL,
    name varchar(10) NOT NULL,
    phonenumber char(13) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    identity_photo varchar(100) NOT NULL,
    student_council_fee_photo varchar(100) NOT NULL,
    account_number varchar(20) NOT NULL,
    state varchar(10) default 'PENDING',
    rejectReason varchar(100)
);

create table request (
    id int NOT NULL primary key auto_increment,
    item_id int NOT NULL,
    borrower_id varchar(50) NOT NULL,
    foreign key(item_id) references item(id),
    foreign key(borrower_id) references borrower(id),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_at datetime NOT NULL,
    borrower_at datetime NOT NULL,
    type varchar(6) NOT NULL,
    state varchar(10) default 'PENDING',
    cancel boolean default false
);

create table response(
    id int NOT NULL primary key auto_increment,
    request_id int NOT NULL,
    admin_id varchar(20) NOT NULL,
    foreign key(request_id) references request(id),
    foreign key(admin_id) references admin(id),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reject_reason varchar(100) NOT NULL,
    type char(6) NOT NULL
);