"use client";

import NoticeContext from "@/context/NoticeContext";
import { dateFormatter } from "@/utilities/dateFormatter";
import positionConvertor from "@/utilities/positionConvertor";
import { useContext } from "react";

export default function NoticeSinglePage({ noticeId }: { noticeId: string }) {
    const { noticeList } = useContext(NoticeContext);

    const current = noticeList.find((notice) => String(notice.id) == noticeId)!;

    const { id, authorId, title, content, updatedAt, postedAt, adminName, adminPosition } = current;

    return (
        <div className="bg-white rounded-xl border border-boxBorder py-5 px-4 mt-5">
            <p className="bold-20px">{title}</p>
            <p className="regular-14px">글번호 : {id}</p>
            <p className="regular-14px">
                작성자 : {positionConvertor(adminPosition)} {adminName}({authorId})
            </p>
            <p className="text-placeholder regular-14px">
                게시일시 : {dateFormatter(postedAt)}
                {postedAt != updatedAt ? `(${dateFormatter(updatedAt)} 최종수정)` : null}
            </p>
            <p className="leading-tight line-clamp-2 mt-3 whitespace-pre-wrap">{content}</p>
        </div>
    );
}
