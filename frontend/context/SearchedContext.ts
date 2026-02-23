import SearchedBorrowersContextInteface from "@/types/SearchedBorrowersContextInteface";
import { createContext } from "react";

const SearchedBorrowersContext = createContext<SearchedBorrowersContextInteface>({
    searchedBorrowers: [],
    setSearchedBorrowers: null,
});

export default SearchedBorrowersContext;
