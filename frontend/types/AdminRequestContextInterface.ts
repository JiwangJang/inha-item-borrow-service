import RequestInterface from "./RequestInterface";

export default interface AdminRequestContextInterface {
    requestList: RequestInterface[];
    setRequestList: React.Dispatch<React.SetStateAction<RequestInterface[]>> | null;
}
