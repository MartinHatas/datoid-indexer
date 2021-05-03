import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import {SERVER_URL} from "./config";

class App extends Component {

    state = {
        query: '',
        result: null
    };

    setQuery = e => this.setState({query: e.target.value});

    doSearch = e => {
        e.preventDefault();
        var queryParam = this.state.query ? "?q=" + this.state.query : ""
        fetch(`${SERVER_URL}/_search` + queryParam)
            .then(r => r.json())
            .then(r => this.setState({result: r}))
            .catch(e => console.error(e))
    };

    render() {
        return (
            <div className="App">
                <header className="App-header">
                    <img src={logo} className="App-logo" alt="logo" />

                    <form onSubmit={this.doSearch}>
                        <input type="text" value={this.state.query} onChange={this.setQuery} />
                        <input type="submit" value="Search" />
                    </form>

                    <p>
                        { this.state.result ?
                            this.state.result.hits.hits.length > 0 ?
                                <div className="App-results">
                                    <ul>
                                        {
                                            this.state.result.hits.hits.map((item, idx) => {
                                                return <li key={idx}><a href={item._source.link} target="_blank">{item._source.filename}</a></li>
                                            })
                                        }
                                    </ul>
                                </div> :
                                <span>No results.</span>
                            : <span/>
                        }
                    </p>
                </header>
            </div>
        );
    }
}

export default App;