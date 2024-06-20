import React, { useState } from "react";

export default function Postform({ onSubmit }) {
  const [username, setUsername] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/${username}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch user data");
      }

      const data = await response.json();
      onSubmit(data);
      setUsername("");
    } catch (error) {
      console.error("Error:", error.message);
      setUsername("");
      onSubmit({ Message: "Error,Key does not exist" });
    }
  };
  return (
    <>
      <form
        onSubmit={handleSubmit}
        className=" bg-cyan-700 p-4 rounded-xl h-80 w-72 m-4"
      >
        <div className="text-white bg-gray-700 w-fit p-1 rounded-md font-mono mb-4 ">
          DELETE
        </div>
        <div className="mb-5">
          <label className="block mb-2 text-lg font-medium font-mono text-gray-900 dark:text-white">
            Username
          </label>
          <input
            onChange={(e) => setUsername(e.target.value)}
            value={username}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg block w-full p-2.5"
          />
        </div>

        <button
          type="submit"
          className=" text-white bg-green-500 ml-32 font-bold py-2 px-4 mt-24 rounded-xl"
        >
          Submit
        </button>
      </form>
    </>
  );
}
