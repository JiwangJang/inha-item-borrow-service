import NoticeInterface from "@/types/NoticeInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { useRouter } from "next/navigation";

export default function NoticeListItem({ notice }: { notice: NoticeInterface }) {
    const router = useRouter();

    return (
        <div
            className="bg-white rounded-xl border border-boxBorder py-5 px-4 cursor-pointer"
            onClick={() => router.push(`/notice/${notice.id}`)}
        >
            <div className="flex justify-between items-center">
                <p className="bold-18px">{notice.title}</p>
                <p className="text-placeholder regular-14px">{dateFormatter(notice.postedAt)}</p>
            </div>
            <p className="leading-tight line-clamp-2">{notice.content}</p>
        </div>
    );
}
