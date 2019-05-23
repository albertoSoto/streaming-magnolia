import React, {Component} from 'react';
import * as uriUtils from './constants';
import ReactDOM from "react-dom";
import StreamingGrid from "./components/StreamingGrid";
import StreamingPlayer from "./components/StreamingPlayer";

class LayoutDisposer extends Component {
    constructor(props) {
        super(props);
        this.state = {streamingData: []};
        this._doRegisterPlay = this._doRegisterPlay.bind(this);
    }

    _doRegisterPlay(o) {
        this.refs.innerPlayer._doPlay(o);
    }

    componentDidMount() {
        fetch(uriUtils.MAGNOLIA_STREAMING_LINKS)
            .then(response => response.json())
            .then(json => {
                this.setState({streamingData: json.data});
                console.log(this.state.streamingData);
            });
    }

    render() {
        return (
            <>
            <div className="col-md-12 alert alert-light" role="alert">
                <StreamingPlayer ref="innerPlayer"/>
            </div>
            <StreamingGrid content={this.state.streamingData} onPlay={this._doRegisterPlay}/>
            </>
        );
    }
}

ReactDOM.render(<LayoutDisposer/>, document.getElementById('streaming-grid'));
//document.querySelector('.videoElement'))
