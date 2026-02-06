export default function Layout({ children }: { children: React.ReactNode }) {
    return <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">{children}</div>;
}
