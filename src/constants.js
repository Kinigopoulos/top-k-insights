exports.__timeColumns = [
    "__time_Year",
    "__time_Month",
    "__time_Day",
    "__time_DayOfWeek",
    "__time_Hour",
    "__time_Minute",
    "__time_Second"
];

exports.__timeColumnsFunctions = {
    "__time_Year": (timestamp) => {
        return new Date(timestamp).getFullYear();
    },
    "__time_Month": (timestamp) => {
        return new Date(timestamp).getMonth();
    },
    "__time_Day": (timestamp) => {
        return new Date(timestamp).getDate();
    },
    "__time_DayOfWeek": (timestamp) => {
        return new Date(timestamp).getDay();
    },
    "__time_Hour": (timestamp) => {
        return new Date(timestamp).getHours();
    },
    "__time_Minute": (timestamp) => {
        return new Date(timestamp).getMinutes();
    },
    "__time_Second": (timestamp) => {
        return new Date(timestamp).getSeconds();
    }
}