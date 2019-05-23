import React, {Component} from 'react';
import * as uriUtils from '../constants';
import PropTypes from "prop-types";

class StreamingItem extends Component {
    render() {
        const item = this.props.obj;
        let imgLink = "/assets/img/faces/face-1.jpg";
        const bgImage = "https://ununsplash.imgix.net/photo-1431578500526-4d9613015464?fit=crop&fm=jpg&h=200&q=75&w=200";
        let isEmpty = !item.imageLink;
        if (!isEmpty) {
            console.log(item.imageLink);
            imgLink = uriUtils.MAGNOLIA_ROOT + item.imageLink;
        }
        let button = "";
        if (item.videoLink) {
            //            {item.videoLink}            {item.videoType}
            button = <button type="button" className="btn btn-link " onClick={() => this.props.onPlay(item)}>Play
            </button>;
        }
        return (<div className="card card-user">
                <div className="image">
                    <img src={bgImage} height={300} width={400} alt="..."/>
                </div>
                <div className="content">
                    <div className="author">
                        <a href="#">
                            <img className="avatar border-gray" src={imgLink} alt="..."/>
                            <h4 className="title">
                                {item.name}
                                <br/>
                                <small>{item.title}</small>
                            </h4>
                        </a>
                    </div>
                    <p className="description text-center">
                        {item.description}
                        <br/>
                        {item.cast}
                        <br/>
                        {button}
                    </p>
                </div>
                <hr/>
                <div className="text-center">
                    {item.imdb}
                </div>
            </div>
        );
    }
}

StreamingItem.propTypes = {
    obj: PropTypes.object,
    onPlay: PropTypes.func.isRequired
};

class StreamingGrid extends Component {
    render() {
        return (<>
            {this.props.content.map(i => {
                return <div className="col-md-2" key={"col" + i.id}>
                    <StreamingItem obj={i} onPlay={this.props.onPlay}/>
                </div>
            })}
            </>
        );
    }
}

StreamingGrid.propTypes = {
    title: PropTypes.string,
    content: PropTypes.array,
    onPlay: PropTypes.func.isRequired
};

StreamingGrid.defaultProps = {
    title: "",
    content: []
};
export default (StreamingGrid);