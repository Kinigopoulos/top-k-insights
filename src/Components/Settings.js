import React, {useState} from "react";
import "./WindowComponent.css";

function Settings({ports, setPorts}) {

    const [settings, setSettings] = useState(ports);

    function setPort(e) {
        e.preventDefault();
        setSettings({...settings, [e.target.name]: e.target.value});
    }

    function apply(e){
        e.preventDefault();
        setPorts(settings);
    }

    return (
        <div className="openDialogContainer">
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
            <button onClick={apply}>Apply</button>
        </div>
    )
}

export default Settings;