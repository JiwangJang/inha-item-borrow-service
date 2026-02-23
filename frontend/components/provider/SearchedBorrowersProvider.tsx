"use client";

import SearchedBorrowersContext from "@/context/SearchedContext";
import BorrowerInfoInterface from "@/types/BorrowerInfoInterface";
import { useState } from "react";

export default function SearchedBorrowersProvider({ children }: { children: React.ReactNode }) {
    const [searchedBorrowers, setSearchedBorrowers] = useState<BorrowerInfoInterface[]>([]);

    return (
        <SearchedBorrowersContext.Provider value={{ searchedBorrowers, setSearchedBorrowers }}>
            {children}
        </SearchedBorrowersContext.Provider>
    );
}
