import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/layout/header/Header";
import BottomNavigator from "@/components/layout/bottom-nav/BottomNavigator";
import NoticeProvider from "@/components/provider/NoticeProvider";
import getNotices from "@/utilities/getNotices";
import getDivisionList from "@/utilities/getDivisionList";
import DivisionProvider from "@/components/provider/DivisionProvider";

export const metadata: Metadata = {
    title: "미래융합대학 물품대여시스템",
    description: "미래융합대학 물품대여시스템",
};

export default async function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const noticeProviderInitialValue = await getNotices();
    const divisionList = await getDivisionList();

    return (
        <html lang="ko">
            <body className="bg-slate-950 max-w-125 min-h-dvh my-0 mx-auto relative flex flex-col">
                <Header />
                <NoticeProvider initialValue={noticeProviderInitialValue}>
                    <DivisionProvider initialValue={divisionList}>
                        <div className="flex-1">{children}</div>
                    </DivisionProvider>
                </NoticeProvider>
                <BottomNavigator />
            </body>
        </html>
    );
}
