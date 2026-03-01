import NoticeSinglePage from "@/components/borrower/notice/single/NoticeSinglePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <NoticeSinglePage noticeId={id} />;
}
