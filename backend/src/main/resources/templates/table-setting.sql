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

INSERT INTO division(code, name) VALUE("TEST", "테스트 부서")

INSERT INTO admin(id, password, email, name, phonenumber, position, division)
    VALUE("test_admin", "$2a$10$SFzLKBUxk9wZ0Tbolo6pUuCi026zyM5L5vtGeiPJXuM21vDTdfrwS", "dd", "test_admin", "999", "PRESIDENT", "TEST");

-- admin table 생성
CREATE TABLE admin(
    id varchar(20) NOT NULL primary key,
    password varchar(60) NOT NULL,
    name varchar(10) NOT NULL,
    position varchar(15) NOT NULL,
    division varchar(20) NOT NULL,
    foreign key (position) references admin_role(role),
    foreign key (division) references division(code),
    is_delete boolean default false
);

-- borrower table 생성
CREATE TABLE borrower(
    -- id : 학번이니깐 8자 고정
    id char(8) NOT NULL primary key,
    -- 혹시모르니 10자로 설정
    name varchar(10) NOT NULL,
    phone_number char(13) NOT NULL,
    account_number varchar(20) NOT NULL,
    ban boolean default false,
    department varchar(50) NOT NULL,
    ban_reason TEXT
);

CREATE TABLE student_council_fee(
    id int PRIMARY KEY auto_increment,
    borrower_id char(8) UNIQUE,
    foreign key(borrower_id) references borrower(id),
    s3_link TEXT,
    request_at datetime ON UPDATE CURRENT_TIMESTAMP,
    response_at datetime,
    deny_reason varchar(50),
    -- 소문자로 수정함
    verify boolean
);

CREATE TABLE borrower_privacy_agreement(
    id int NOT NULL primary key auto_increment,
    borrower_id char(8) UNIQUE,
    foreign key(borrower_id) references borrower(id),
    agreed_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version char(2) NOT NULL
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

-- manager NOT NULL 로 되있었지만 기존 테스트 충돌로 인해 임시로 지움
create table request (
    id int NOT NULL primary key auto_increment,
    item_id int NOT NULL,
    borrower_id varchar(50) NOT NULL,
    manager varchar(20) DEFAULT NULL,
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