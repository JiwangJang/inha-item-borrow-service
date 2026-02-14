import BorrowerItemProvider from "@/components/provider/BorrowerItemProvider";
import BorrowerProvider from "@/components/provider/BorrowerProvider";
import BorrowRequestProvider from "@/components/provider/BorrowRequestProvider";
import { mockRequests } from "@/mockData/mockRequest";
import checkBorrowLogin from "@/utilities/checkBorrowLogin";
import getItems from "@/utilities/getItems";
import getRequests from "@/utilities/getRequests";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const borrowerInfo = await checkBorrowLogin();
    const initialItemList = await getItems();

    const requests = await getRequests();
    // const requests = mockRequests;

    return (
        <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">
            <BorrowerItemProvider initialValue={initialItemList}>
                <BorrowRequestProvider initialValue={requests}>
                    <BorrowerProvider initialBorrowerInfo={borrowerInfo}>{children}</BorrowerProvider>
                </BorrowRequestProvider>
            </BorrowerItemProvider>
        </div>
    );
}
