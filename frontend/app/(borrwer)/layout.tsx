import BorrowerItemProvider from "@/components/provider/BorrowerItemProvider";
import BorrowerProvider from "@/components/provider/BorrowerProvider";
import checkBorrowLogin from "@/utilities/checkBorrowLogin";
import getItems from "@/utilities/getItems";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const borrowerInfo = await checkBorrowLogin();
    const initialItemList = await getItems();

    return (
        <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">
            <BorrowerItemProvider initialValue={initialItemList}>
                <BorrowerProvider initialBorrowerInfo={borrowerInfo}>{children}</BorrowerProvider>
            </BorrowerItemProvider>
        </div>
    );
}
