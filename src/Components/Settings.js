import React, {useState} from "react";
import "./WindowComponent.css";

function Settings({ports, setPorts, credentials, setCredentials}) {

    const [settings, setSettings] = useState(ports || {});
    const [localCredentials, setLocalCredentials] = useState(credentials || {});

    function setPort(e) {
        e.preventDefault();
        const newSettings = {...settings, [e.target.name]: e.target.value};
        setSettings(newSettings);
    }

    function setCredential(e) {
        e.preventDefault();
        const newSettings = {...localCredentials, [e.target.name]: e.target.value};
        setLocalCredentials(newSettings);
    }

    function apply(e) {
        e.preventDefault();
        setPorts(settings);
        setCredentials(localCredentials);
        localStorage.setItem("ports", JSON.stringify(settings));
        localStorage.setItem("credentials", JSON.stringify(localCredentials));
        window.location.reload();
    }

    return (
        <div className="openDialogContainer">
            <h1>Settings</h1>
            <h2>Set Up Your Druid Ports</h2>
            {
                Object.entries(settings).map(([setting, value]) => {
                    return (
                        <div key={setting}>
                            <span>{setting.toString()}</span>
                            <input name={setting} type="text" onChange={setPort} value={value}/>

                        </div>
                    )
                })
            }

            <h2>Set Up Your Credentials</h2>
            {
                Object.entries(localCredentials).map(([setting, value]) => {
                    return (
                        <div key={setting}>
                            <span>{setting.toString()}</span>
                            <input name={setting} type={setting === "password" ? "password" : "text"} onChange={setCredential} value={value}/>

                        </div>
                    )
                })
            }
            <button onClick={apply}>Apply</button>
        </div>
    )
}

export default Settings;