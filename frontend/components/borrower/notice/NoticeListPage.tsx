"use client";

import NoticeContext from "@/context/NoticeContext";
import { useContext } from "react";
import NoticeListItem from "./NoticeListItem";

export default function NoticeListPage() {
    const { noticeList } = useContext(NoticeContext);

    return (
        <div className="mt-5">
            <div>
                <p className="black-20px">📣 공지사항</p>
                <p>공지사항에 관련된 문의는 학생회 공식 이메일로 해주시면 되겠습니다.</p>
            </div>
            <div className="mt-3 flex flex-col gap-1">
                {noticeList.map((notice) => (
                    <NoticeListItem notice={notice} key={notice.id} />
                ))}
            </div>
        </div>
    );
}
