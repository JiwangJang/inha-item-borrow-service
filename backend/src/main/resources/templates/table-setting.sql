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

INSERT INTO admin_role (role, level)
VALUES ('PRESIDENT', 4), ('VICE_PRESIDENT', 3), ('DIVISION_HEAD', 2), ('DIVISION_MEMBER', 1);

INSERT INTO division(code, name) VALUE("TEST", "테스트 부서");

INSERT INTO admin(id, password, name, position, division)
    VALUE("test_admin", "$2a$10$SFzLKBUxk9wZ0Tbolo6pUuCi026zyM5L5vtGeiPJXuM21vDTdfrwS", "test_admin", "PRESIDENT", "TEST");

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
    verify boolean DEFAULT false
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



-- 학생회 감사대비 쿼리
SELECT
        rq_a.id AS 대여요청번호,
        rq_a.item_id  AS 대여물품번호,
        item.name AS 대여물품명,
        item.price AS 대여물품가격,
        rq_a.created_at AS 대여요청시각,
        rq_a.borrower_id AS 대여자학번,
        borrower.name AS 대여자,
        rq_a.return_at AS "반납시각(예상)",
        rq_a.borrow_at AS 대여시각,
        rq_a.type AS 요청구분,
        rq_a.state AS 상태,
        rp.id AS 응답번호,
        rp.created_at AS 응답시각,
        rp.reject_reason AS 거절사유,
        admin.name AS 담당자,
        admin.position AS 담당자직급,
        rq_b.id AS 반납요청번호,
        rq_b.type AS 반납요청구분,
        rq_b.state AS 반납요청상태,
        rq_b.created_at AS 반납요청시각,
        rq_b.return_at AS 반납시각
    FROM request AS rq_a
        -- 대여요청과 반납요청간 짝지어주는 부분
        -- 대여시간이 동일하고 아이디와 티입이 다른 것을 묶는다
        -- 이때 대여요청(BORROW)이면서 상태가 거절(REJECT)인 것은 제외하고 조인
        LEFT JOIN request AS rq_b
            ON rq_a.borrow_at = rq_b.borrow_at
                AND rq_a.id != rq_b.id
                AND rq_a.type != rq_b.type
                AND NOT(rq_b.type = "BORROW" AND rq_b.state = "REJECT")
        LEFT JOIN response AS rp
            ON rp.request_id = rq_a.id
        LEFT JOIN admin
            ON admin.id = rq_a.manager
        LEFT JOIN item
            ON rq_a.item_id = item.id
        LEFT JOIN borrower
            ON rq_a.borrower_id = borrower.id
    WHERE rq_a.cancel != true AND rq_a.state = "PERMIT" AND rq_a.type ="BORROW";