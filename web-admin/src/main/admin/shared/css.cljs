(ns admin.shared.css)

(def main-container "flex-1 border flex-col p-2 bg-white h-auto w-full")
(def page-title "text-gray-700 text-2xl font-medium border-b")

(def btn-base "focus:ring-4 focus:outline-none focus:ring-gray-300 font-medium rounded-lg px-3 py-2 text-xs 
               text-center mr-2 mb-2 border hover:text-white dark:hover:text-white")

;; Button
(def button-default (str btn-base " text-blue-700 border-blue-700 hover:bg-blue-800 
                      dark:border-blue-500 dark:text-blue-500 dark:hover:bg-blue-500 dark:focus:ring-blue-800"))

(def button-dark  (str btn-base " text-gray-900 hover:text-white border-gray-800 hover:bg-gray-900 
                   dark:border-gray-600 dark:text-gray-400 dark:hover:bg-gray-600 dark:focus:ring-gray-800"))

(def button-green (str btn-base " text-green-700 hover:text-white border border-green-700 hover:bg-green-800 
                   dark:border-green-500 dark:text-green-500 dark:hover:bg-green-600 dark:focus:ring-green-800"))

(def button-red (str btn-base " text-red-700 border-red-700 hover:bg-red-800  
                               dark:border-red-500 dark:text-red-500 dark:hover:bg-red-600 dark:focus:ring-red-900"))

(def button-yellow (str btn-base "text-yellow-400 border-yellow-400 hover:bg-yellow-500 dark:border-yellow-300 
                                  dark:text-yellow-300 dark:hover:bg-yellow-400 dark:focus:ring-yellow-900"))

(def buton-purple (str btn-base " text-purple-700 border-purple-700 hover:bg-purple-800 dark:border-purple-400 
                                 dark:text-purple-400 dark:hover:bg-purple-500 dark:focus:ring-purple-900"))

(def btn-edit "font-medium text-blue-600 dark:text-blue-500 hover:underline")
(def btn-del "font-medium text-red-600 dark:text-red-500 hover:underline")

(def divi "m-1 p-1")