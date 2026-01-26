CREATE schema inha_item_borrow_service;

use inha_item_borrow_service;


CREATE TABLE admin_role(
    role varchar(15) primary key,
    level int NOT NULL
);

CREATE TABLE division(
    code varchar(20) primary key,
    name varchar(20) NOT NULL,
    is_delete boolean default false
);

INSERT INTO admin_role(role, level) VALUE("PRESIDENT", 4);
INSERT INTO admin_role(role, level) VALUE("VICE_PRESIDENT", 3);
INSERT INTO admin_role(role, level) VALUE("DIVISION_HEAD", 2);
INSERT INTO admin_role(role, level) VALUE("DIVISION_MEMBER", 1);

INSERT INTO division(code, name) VALUE("TEST", "테스트 부서");

INSERT INTO admin(id, password, email, name, phonenumber, position, division)
    VALUE("test", "$2a$10$SFzLKBUxk9wZ0Tbolo6pUuCi026zyM5L5vtGeiPJXuM21vDTdfrwS", "dd", "테스터", "999", "PRESIDENT", "TEST");

-- admin table 생성
CREATE TABLE admin(
    id varchar(20) NOT NULL primary key,
    password varchar(60) NOT NULL,
    email varchar(50) NOT NULL,
    name varchar(10) NOT NULL,
    phonenumber char(13) NOT NULL,
    position varchar(15) NOT NULL,
    division varchar(20) NOT NULL,
    foreign key (position) references admin_role(role),
    foreign key (division) references division(code),
    is_delete boolean default false
);

-- borrower table 생성
CREATE TABLE borrower(
    id char(8) NOT NULL primary key,
    name varchar(10) NOT NULL,
    department varchar(10) NOT NULL,
    phone_number char(13) NOT NULL,
    account_number varchar(20) NOT NULL,
    ban boolean default false
);

CREATE TABLE student_council_fee(
    id char(8) NOT NULL,
    foreign key(id) references borrower(id),
    verify boolean default false,
    s3_link varchar(100),
    request_at datetime,
    response_at datetime, 
    deny_reason TEXT
);

CREATE TABLE borrower_privacy_agreement(
    id int NOT NULL primary key auto_increment,
    borrower_id char(8) NOT NULL,
    foreign key(borrower_id) references borrower(id),
    agreed_at datetime NOT NULL,
    version char(2) NOT NULL
);

CREATE TABLE notification(
    id int NOT NULL PRIMARY KEY auto_increment,
    is_read boolean DEFAULT false,
    content TEXT NOT NULL,
    target_id char(8) NOT NULL,
    foreign key(target_id) references borrower(id),
    notify_at DATETIME default CURRENT_TIMESTAMP
);

create table notice (
    id int NOT NULL PRIMARY KEY auto_increment,
    title varchar(50) NOT NULL,
    content TEXT NOT NULL,
    posted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    author_id varchar(20) NOT NULL,
    foreign key(author_id) references admin(id)
);

create table item (
    id int NOT NULL PRIMARY KEY auto_increment,
    name varchar(10) NOT NULL,
    location varchar(50) NOT NULL,
    password varchar(8) NOT NULL,
    delete_reason varchar(50),
    price int NOT NULL,
    state varchar(9) NOT NULL default 'AFFORD'
);

create table request (
    id int NOT NULL primary key auto_increment,
    item_id int NOT NULL,
    borrower_id char(8) NOT NULL,
    manager varchar(20) NOT NULL,
    foreign key(manager) references admin(id),
    foreign key(item_id) references item(id),
    foreign key(borrower_id) references borrower(id),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_at datetime NOT NULL,
    borrow_at datetime NOT NULL,
    type varchar(6) NOT NULL,
    state varchar(10) default 'PENDING',
    cancel boolean default false
);

create table response(
    id int NOT NULL primary key auto_increment,
    request_id int NOT NULL,
    foreign key(request_id) references request(id),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reject_reason varchar(100),
    type char(6) NOT NULL
);