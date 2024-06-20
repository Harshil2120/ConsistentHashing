import React, { useState } from "react";

export default function Postform({ onSubmit }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log(username, email);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/add`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ username: username, email: email }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to submit form");
      }

      const data = await response.json();
      onSubmit(data);
      setUsername("");
      setEmail("");
    } catch (error) {
      console.error("Error:", error.message);
      setUsername("");
      setEmail("");
      onSubmit({ Message: "Error, Cannot Submit" });
    }
  };
  return (
    <>
      <form
        onSubmit={handleSubmit}
        className="w-72 bg-cyan-700 p-4 rounded-xl h-80 m-4"
      >
        <div className="text-white bg-gray-700 w-fit p-1 rounded-md font-mono mb-4 ">
          POST
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
        <div className="mb-5">
          <label className="block mb-2 text-lg font-medium font-mono text-gray-900 dark:text-white">
            Email
          </label>
          <input
            onChange={(e) => setEmail(e.target.value)}
            value={email}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg block w-full p-2.5"
          />
        </div>

        <button
          type="submit"
          className=" text-white bg-green-500 ml-32 font-bold py-2 px-4 rounded-xl"
        >
          Submit
        </button>
      </form>
    </>
  );
}
