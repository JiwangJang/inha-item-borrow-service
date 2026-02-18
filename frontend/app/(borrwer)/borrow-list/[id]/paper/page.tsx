import BorrowRequestPaperPage from "@/components/borrower/borrow-list/single/paper/BorrowRequestPaperPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <BorrowRequestPaperPage requestId={id} />;
}
