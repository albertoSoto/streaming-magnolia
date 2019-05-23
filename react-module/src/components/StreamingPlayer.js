import React, {Component} from 'react';
import {
    Player, ControlBar, PlaybackRateMenuButton, ReplayControl, VolumeMenuButton
} from 'video-react';
import * as uriUtils from '../constants';

class StreamingPlayer extends Component {
    constructor(props) {
        super(props);
        this._doPlay = this._doPlay.bind(this);
        this._doClose = this._doClose.bind(this);
        this.state = {
            isShown: false, media: {
                poster: "https://video-react.js.org/assets/poster.png",
                source: "https://media.w3.org/2010/05/sintel/trailer_hd.mp4",
                type: "video/mp4"
            }
        };
    }

    componentDidMount() {
        // instantiate Video.js
/*        this.player = videojs(this.videoNode, this.props, function onPlayerReady() {
            console.log('onPlayerReady', this)
        });*/
    }
    componentWillUnmount() {
        // destroy player on unmount
        if (this.player) {
            this.player.dispose()
        }
    }

    _doClose() {
        this.player.pause();
        this.setState({isShown: false});
    }

    _doPlay(o) {
        const newMedia = {
            poster: uriUtils.MAGNOLIA_ROOT + o.imageLink,
            source: uriUtils.MAGNOLIA_ROOT + "/" + o.videoLink,
            type: o.videoType
        };
        console.log(newMedia);
        this.setState({isShown: true, media: newMedia});
        //this.player.update();
        console.log(this.state);
    }

    render() {
        const statusClass = this.state.isShown ? "" : "hidden";
        let {poster, source, type} = this.state.media;
        console.log(source);
        return (
            <div className={statusClass}>
                <button type="button" className="btn btn-link " onClick={() => this._doClose()}>Close</button>
                <Player
                    ref={(c) => {
                        this.player = c;
                    }}
                    autoPlay={false}
                    playsInline
                    poster={poster}
                    src={source}
                >

                    <ControlBar autoHide={false}>
                        <VolumeMenuButton vertical={true}/>
                        <PlaybackRateMenuButton rates={[5, 2, 1, 0.5, 0.1]}/>
                        <ReplayControl seconds={5} order={2.1}/>
                    </ControlBar>
                </Player>
            </div>
        );
    }
}

export default (StreamingPlayer);
