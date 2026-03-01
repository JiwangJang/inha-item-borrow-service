import RequestInterface from "./RequestInterface";

export default interface RequestContextInterface {
    requestList: RequestInterface[];
    setRequestList: React.Dispatch<React.SetStateAction<RequestInterface[]>> | null;
}
