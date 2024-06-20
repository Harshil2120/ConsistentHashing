import Postform from "@/components/Postform";
import Putform from "@/components/Putform";
import Getform from "@/components/Getform";
import Deleteform from "@/components/Deleteform";
import { useState } from "react";


export default function Home() {
  const [userData, setUserData] = useState([]);

  const handlepostFormSubmit = (data) => {
    setUserData(data);
  };

  const handlegetFormSubmit = (data) => {
    setUserData(data);
  };

  const handleputFormSubmit = (data) => {
    setUserData(data);
  };

  const handledeleteFormSubmit = (data) => {
    setUserData(data);
  };


  return (
    <>
    <div className="h-screen w-screen">
      <nav className="fixed top-0 z-50 w-full border-b-2 border-gray-100 border-dashed bg-white dark:border-gray-700 dark:bg-gray-800">
        <div className="px-2 py-3">
          <div className="flex items-center justify-start rtl:justify-end">
            <div className="flex space-x-3">
              <img src="/logo-footer.png" className=" w-12 h-12 p-1" />
              <div className="border-l-2 border-gray-400"></div>
              <img src="/consistent_logo.png" className=" w-12 h-12 p-1" />
              <span className="text-xl font-mono font-semibold pt-3 text-white">
                Consistent Hashing
              </span>
            </div>
          </div>
        </div>
      </nav>


      <div className="h-full bg-gray-800 w-full py-24 px-4">
        <div className="bg-gray-700 p-1 flex flex-wrap items-center justify-center rounded-xl">
       

        <Postform onSubmit={handlepostFormSubmit}/>


       <Putform onSubmit={handleputFormSubmit}/>


      <Getform onSubmit={handlegetFormSubmit}/>

      <Deleteform onSubmit={handledeleteFormSubmit}/>
        </div>

        <div className="bg-white h-28 mt-4 rounded-xl p-3">
        {userData && (
          <>
          <div>
              <p>{userData.ServerId}</p>
            </div>
            <div>
            <p>{userData.Email}</p>
          </div>
            <div>
            <p>{userData.Message}</p>
          </div>
          </>   
          )}
        </div>
        
      </div>
    
  </div>

    </>
  );
}
