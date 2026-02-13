import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/layout/header/Header";
import BottomNavigator from "@/components/layout/bottom-nav/BottomNavigator";
import ItemProvider from "@/components/provider/ItemProvider";
import NoticeProvider from "@/components/provider/NoticeProvider";
import getItems from "@/utilities/getItems";
import getNotices from "@/utilities/getNotices";

export const metadata: Metadata = {
    title: "미래융합대학 물품대여시스템",
    description: "미래융합대학 물품대여시스템",
};

export default async function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const itemProviderInitialValue = await getItems();
    const noticeProviderInitialValue = await getNotices();

    return (
        <html lang="ko">
            <body className="bg-slate-950 max-w-125 h-screen my-0 mx-auto relative flex flex-col overflow-hidden">
                <Header />
                <NoticeProvider initialValue={noticeProviderInitialValue}>
                    <ItemProvider initialValue={itemProviderInitialValue}>
                        <div className="flex-1 overflow-y-auto">{children}</div>
                    </ItemProvider>
                </NoticeProvider>
                <BottomNavigator />
            </body>
        </html>
    );
}
