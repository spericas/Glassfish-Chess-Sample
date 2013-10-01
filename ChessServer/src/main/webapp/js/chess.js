/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/**
 * Chess HTML5 Client.
 * 
 * @author Jan Stola
 */

Settings = function() {
    var self = this;

    self.username = ko.observable(null); // String
    self.password = ko.observable(null); // String
    self.url = ko.observable(); // String

    self.validCredentials = ko.computed(function() {
        var username = self.username();
        var password = self.password();
        return (username !== null) && (username.length !== 0)
            && (password !== null) && (password.length !== 0);
    });

    self.read = function() {
        self.load('username', '');
        self.load('password', '');
        var hostname = (location.protocol === 'file:') ? 'localhost' : location.hostname;
        self.load('url', 'ws://' + hostname + ':8080/chess/chessserver');
    };

    self.write = function() {
        self.save('username');
        self.save('password');
        self.save('url');
    };

    self.load = function(property, defaultValue) {
        var key = Settings.STORAGE_PREFIX+property;
        var value = localStorage[key];
        if (!value) {
            value = defaultValue;
        }
        self[property](value);
    };

    self.save = function(property) {
        var key = Settings.STORAGE_PREFIX+property;
        var value = self[property]();
        localStorage[key] = value;
    };
};

Settings.STORAGE_PREFIX = 'ChessHTML5Client.';

Game = function(obj) {
    var self = this;

    self.gameId = ko.observable(); // String
    self.summary = ko.observable(null); // String
    self.open = ko.observable(); // boolean
    self.whitePlayer = ko.observable(); // String
    self.blackPlayer = ko.observable(); // String
    self.own = ko.observable(); // boolean

    self.description = ko.computed(function() {
        return (self.summary() === null) ? self.gameId() : self.summary();
    });

    self.joinable = ko.computed(function() {
        return self.open() || self.own();
    });
    
    if (obj) {
        ko.mapping.fromJS(obj, {}, self);
    }
};

Games = function() {
    var self = this;

    self.games = ko.observableArray([]); // Game[]
    self.selectedColor = ko.observable(); // String

    self.anyOwnGame = ko.computed(function() {
        var games = self.games();
        for (var i=0; i<games.length; i++) {
            if (games[i].own()) {
                return true;
            }
        }
        return false;
    });

    self.anyOtherGame = ko.computed(function() {
        var games = self.games();
        for (var i=0; i<games.length; i++) {
            if (!games[i].own()) {
                return true;
            }
        }
        return false;
    });

};

MsgType = {
    CreateGame: 'CreateGame',
    QueryGames: 'QueryGames',
    SendMove: 'SendMove',
    JoinGame: 'JoinGame',
    UpdateGame: 'UpdateGame',
    CheckCredentials: 'CheckCredentials',
    QueryMoves: 'QueryMoves',

    forResponse: function(msg) {
        var suffix = 'Rsp';
        if (msg.indexOf(suffix, msg.length - suffix.length) !== -1) {
            msg = msg.substring(0, msg.length - suffix.length);
        }
        return this[msg];
    }
};

Request = function() {
    var self = this;

    self.msg = ko.observable(); // MsgType
    self.username = ko.observable(); // String
    self.password = ko.observable(); // String
    self.gameId = ko.observable(); // String
    self.color = ko.observable(); // Color
    self.from = ko.observable(); // String
    self.to = ko.observable(); // String
    self.observer = ko.observable(); // boolean
};

AlertType = {
    CHECKMATE: 'CHECKMATE',
    CHECK: 'CHECK',
    DRAW: 'DRAW',

    terminal: function(type) {
        return (type === AlertType.CHECKMATE) || (type === AlertType.DRAW);
    }
};

Alert = function(obj) {
    var self = this;

    self.type = ko.observable(); // AlertType
    self.message = ko.observable(); // String
    
    if (obj) {
        ko.mapping.fromJS(obj, {}, self);
    }
};

Color = {
    WHITE: 'W',
    BLACK: 'B'
};

Board = function() {
    var self = this;

    self.gameId = ko.observable(null); // String
    self.player = ko.observable(); // Color
    self.alert = ko.observable(); // Alert
    self.rows = ko.observableArray(); // Row[]
    self.turn = ko.observable(); // Color
    self.moves = ko.observableArray(); // Move[]
    self.pendingMove = ko.observable(); // Move
    self.active = ko.observable(); // boolean

    self.status = ko.computed(function() {
        var alert = self.alert();
        return alert ? alert.message() : null;
    });

    self.title = ko.computed(function() {
        return self.gameId();     
    });

    self.shortTitle = ko.computed(function() {
        var t = self.gameId();
        if (t && t.length > 10) {
            t = t.substring(0, 10);
        }
        return t;
    });

    self.myTurn = ko.computed(function() {
        return (self.player() === self.turn());
    });

    self.justObserving = ko.computed(function() {
        return (self.player() === null); 
    });

    self.play = function(resource) {
        var audio = new Audio();
        audio.src = resource;
        audio.play();
    };

    self.moves.subscribe(function() {
        self.play('./sounds/move.mp3');
    });

    self.warnCheckAndMate = function() {
        var alert = self.alert();
        if (alert) {
            if (alert.type() === AlertType.CHECK) {
                self.play('./sounds/check.mp3');
            }
            if (alert.type() === AlertType.CHECKMATE) {
                self.play('./sounds/checkmate.mp3');
            }
        }
    };

    self.alert.subscribe(self.warnCheckAndMate);

    self.queryMoves = function(position) {
        var ui = UI.findUI(self);
        if (ui !== null) {
            var sm = new Request();
            sm.msg(MsgType.QueryMoves);
            sm.gameId(self.gameId());
            sm.from(position.location());
            sm.color(self.player());
            ui.sendMsg(sm);
        }
    };

    self.selected = function(/* Square */ data) {
        if (self.turn() !== self.player()) {
            var alert = new Alert();
            alert.message('Not your turn!');
            self.alert(alert);
            return;
        }
        
        var previoslySelected = self.findSelectedSquare();
        if (previoslySelected === null) {
            if (data.piece() !== null && data.pieceColor() === self.turn()) {
                data.selected(true);
                Rules.computeAccessible(self, data);
                self.queryMoves(data.position());
            }
        } else {
            if (previoslySelected === data) {
                data.selected(false);
                Rules.computeAccessible(self, null);
                return;
            }
            if (data.piece() !== null && data.pieceColor() === previoslySelected.pieceColor()) {
                previoslySelected.selected(false);
                data.selected(true);
                Rules.computeAccessible(self, data);
                self.queryMoves(data.position());
                return;
            }
            if (data.accessible()) {
                previoslySelected.selected(false);

                var newMove = new Move();
                newMove.from(previoslySelected.position());
                newMove.to(data.position());
                newMove.round(self.moves().length / 2 + 1);
                newMove.piece(previoslySelected.piece());
                newMove.turn(previoslySelected.pieceColor());
                newMove.takes(data.piece() !== null);
                self.moves.push(newMove);
                self.pendingMove(newMove);

                data.pending(true);
                data.pieceColor(previoslySelected.pieceColor());
                data.piece(previoslySelected.piece());
                self.turn(null);
                previoslySelected.piece(null);
                previoslySelected.pieceColor(null);
                Rules.computeAccessible(self, null);
                
                var ui = UI.findUI(self);
                if (ui !== null) {
                    var sm = new Request();
                    sm.msg(MsgType.SendMove);
                    sm.gameId(self.gameId());
                    sm.from(newMove.from().location());
                    sm.to(newMove.to().location());
                    sm.color(newMove.turn());

                    ui.sendMsg(sm);
                }
            }
        }
    };

    self.showPosition = function(/* Move */ data) {
        Rules.initBoard(self);
        var moves = self.moves();
        for (var i=0; i<moves.length; i++) {
            var m = moves[i];
            var from = self.findSquare(m.from());
            var to = self.findSquare(m.to());
            to.piece(from.piece());
            to.pieceColor(from.pieceColor());
            from.piece(null);
            from.pieceColor(null);
            if (m === data) {
                break;
            }
        }
        self.turn(null);        
    };

    self.rotateBoard = function() {
        self.rows.reverse();
        var rows = self.rows();
        for (var i=0; i<rows.length; i++) {
            rows[i].columns.reverse();
        }
        var sq = self.findSelectedSquare();
        if (sq !== null) {
            sq.selected(false);
            Rules.computeAccessible(self, null);
        }
    };

    self.whiteTurn = ko.computed(function() {
        return self.turn() === Color.WHITE;
    });

    self.blackTurn = ko.computed(function() {
        return self.turn() === Color.BLACK;
    });

    self.columnNames = ko.computed(function() {
        var rows = self.rows();
        var whiteDown = (rows.length === 0) || rows[0].y() === 8;
        var arr = [];
        for (var i = 0; i < 8; i++) {
            var s;
            if (whiteDown) {
                s = String.fromCharCode('A'.charCodeAt(0)+i);
            } else {
                s = String.fromCharCode('H'.charCodeAt(0)-i);
            }
            arr.push(s);
        }
        return arr;
    });
    
    self.findSquare0 = function(column, row) {
        var rows = self.rows();
        for (var i=0; i<rows.length; i++) {
            var columns = rows[i].columns();
            for (var j=0; j<columns.length; j++) {
                var square = columns[j];
                if (square.position().x() === column && square.position().y() === row) {
                    return square;
                }
            }
        }
        return null;
    };

    self.findSquare = function(position) {
        var column = position.x();
        var row = position.y();
        return self.findSquare0(column, row);
    };
    
    self.findSelectedSquare = function() {
        var rows = self.rows();
        for (var i=0; i<rows.length; i++) {
            var columns = rows[i].columns();
            for (var j=0; j<columns.length; j++) {
                var square = columns[j];
                if (square.selected()) {
                    return square;
                }
            }
        }
        return null;
    };

    self.moveResponse = function(/* String */ errMsg, /* String[] */ whites, /* String[] */ blacks,
            /* Color */ turn, /* Alert */ alert) {
        if (errMsg) {
            alert = new Alert();
            alert.message(errMsg);
        }
        if (errMsg !== null) {
            self.moves.remove(self.pendingMove());
            self.pendingMove(null);
        } else {
            self.turn(turn);
        }
        self.alert(alert);
        Rules.initBoard1(self, whites, blacks, turn);
    };

    self.moveUpdate = function(/* Move */ move, /* String[] */ whites, /* String[] */ blacks,
            /* Color */ turn, /* Alert */ alert) {
        var from = self.findSquare(move.from());
        var to = self.findSquare(move.to());
        move.piece(from.piece());
        move.turn(from.pieceColor());
        if (to.piece() !== null) {
            move.takes(true);
        }
        self.alert(alert);
        move.round(self.moves().length / 2 + 1);
        self.moves.push(move);
        Rules.initBoard1(self, whites, blacks, turn);
    };

    self.updateAccessible = function(squares) {
        var rows = self.rows();
        for (var i=0; i<rows.length; i++) {
            var columns = rows[i].columns();
            for (var j=0; j<columns.length; j++) {
                var square = columns[j];
                var location = square.position().location();
                var index = squares.indexOf(location);
                var accessible = (index !== -1);
                square.accessible(accessible);
                if (accessible) {
                    squares.splice(index, 1);
                }
            }
        }
    };

};

Row = function() {
    var self = this;

    self.columns = ko.observableArray(); // Square[]

    self.y = ko.computed(function() {
        var columns = self.columns();
        return (columns.length === 0) ? 0 : columns[0].y();
    });
};

PieceType = {
    PAWN: 5,
    ROCK: 2,
    KNIGHT: 4,
    BISHOP: 3,
    QUEEN: 1,
    KING: 0,

    computeEntity: function(pieceType, color) {
        var base = 18;
        return '&#98' + (base + pieceType) + ';';
    },
    
    fromNotation: function(notation) {
        switch (notation) {
            case 'R': return PieceType.ROCK;
            case 'N': return PieceType.KNIGHT;
            case 'B': return PieceType.BISHOP;
            case 'Q': return PieceType.QUEEN;
            case 'K': return PieceType.KING;
            case 'P': return PieceType.PAWN;
        }
    }

};

Position = function() {
    var self = this;

    self.x = ko.observable('A'); // char
    self.y = ko.observable(); // int
    
    self.location = ko.computed(function() {
        return String.fromCharCode(self.x().charCodeAt(0) - 'A'.charCodeAt(0) + 'a'.charCodeAt(0)) + self.y();
    });
};

Square = function() {
    var self = this;

    self.position = ko.observable(null); // Position
    self.color = ko.observable(null); // Color
    self.piece = ko.observable(null); // PieceType
    self.pieceColor = ko.observable(null); // Color
    self.selected = ko.observable(false); // boolean
    self.accessible = ko.observable(false); // boolean
    self.pending = ko.observable(false); // boolean

    self.pieceEntity = ko.computed(function() {
        var piece = self.piece();
        if (piece === null) {
            return '';
        }
        return PieceType.computeEntity(piece, self.pieceColor());
    });

    self.squareColor = ko.computed(function() {
        if (self.selected()) {
            return 'selected';
        }
        if (self.accessible()) {
            return 'accessible';
        }
        if (self.pending()) {
            return 'pending';
        }

        if (self.color() === null) {
            return '';
        } else {
            if (self.color() === Color.WHITE) {
                return 'white';
            } else {
                return 'black';
            }
        }
    });

    self.x = ko.computed(function() {
        var position = self.position();
        return (position === null) ? 'A' : position.x();
    });

    self.y = ko.computed(function() {
        var position = self.position();
        return (position === null) ? 1 : position.y();
    });
};

Move = function() {
    var self = this;

    self.round = ko.observable(); // int
    self.turn = ko.observable(); // Color
    self.piece = ko.observable(null); // PieceType
    self.from = ko.observable(null); // Position
    self.to = ko.observable(null); // Position
    self.promoted = ko.observable(); // PieceType
    self.takes = ko.observable(); // boolean
    self.check = ko.observable(); // boolean

    self.whiteMove = ko.computed(function() {
        return self.turn() === Color.WHITE;
    });

    self.html = ko.computed(function() {
        if ((self.from() === null) || (self.to() === null)) {
            return '';
        }
        var sb = '';
        var piece = self.piece();
        if (piece !== null && piece !== PieceType.PAWN) {
            sb += PieceType.computeEntity(piece, self.turn());
        }

        sb += self.from().location();
        if (self.takes()) {
            sb += 'x';
        }
        sb += self.to().location();
        return sb;
    });

};

Move.valueOf = function(/* String */ move) {
    move = move.toUpperCase();
    var m = new Move();

    var p = new Position();
    p.x(move.charAt(0));
    p.y(move.charCodeAt(1) - '0'.charCodeAt(0));
    m.from(p);

    p = new Position();
    p.x(move.charAt(2));
    p.y(move.charCodeAt(3) - '0'.charCodeAt(0));
    m.to(p);

    return m;        
};

Rules = {
    computeAccessible: function(/* Board */ b, /* Square */ s) {
        var rows = b.rows();
        for (var i=0; i<rows.length; i++) {
            var columns = rows[i].columns();
            for (var j=0; j<columns.length; j++) {
                columns[j].accessible(false);
            }
        }

        if (s === null) {
            return;
        }
        
        switch (s.piece()) {
            case PieceType.BISHOP: 
                this.moveBishop(b, s);
                break;
            case PieceType.KING:
                this.computeAccessible0(b, s, 1, 1, 1);
                this.computeAccessible0(b, s, 1, -1, 1);
                this.computeAccessible0(b, s, -1, -1, 1);
                this.computeAccessible0(b, s, -1, 1, 1);
                this.computeAccessible0(b, s, 1, 0, 1);
                this.computeAccessible0(b, s, 0, -1, 1);
                this.computeAccessible0(b, s, 0, 1, 1);
                this.computeAccessible0(b, s, -1, 0, 1);
                this.checkRochade(b, s);
                break;
            case PieceType.ROCK:
                this.moveRock(b, s);
                break;
            case PieceType.QUEEN:
                this.moveRock(b, s);
                this.moveBishop(b, s);
                break;
            case PieceType.KNIGHT:
                this.computeAccessible0(b, s, 2, 1, 1);
                this.computeAccessible0(b, s, 2, -1, 1);
                this.computeAccessible0(b, s, -2, -1, 1);
                this.computeAccessible0(b, s, -2, 1, 1);
                this.computeAccessible0(b, s, 1, 2, 1);
                this.computeAccessible0(b, s, -1, 2, 1);
                this.computeAccessible0(b, s, -1, -2, 1);
                this.computeAccessible0(b, s, 1, -2, 1);
                break;
            case PieceType.PAWN:
                this.pawns(b, s);
                break;
        }
    },
    
    moveRock: function(/* Board */ b, /* Square */ s) {
        this.computeAccessible0(b, s, 1, 0, 8);
        this.computeAccessible0(b, s, 0, -1, 8);
        this.computeAccessible0(b, s, -1, 0, 8);
        this.computeAccessible0(b, s, 0, 1, 8);
    },

    moveBishop: function(/* Board */ b, /* Square */ s) {
        this.computeAccessible0(b, s, 1, 1, 8);
        this.computeAccessible0(b, s, 1, -1, 8);
        this.computeAccessible0(b, s, -1, -1, 8);
        this.computeAccessible0(b, s, -1, 1, 8);
    },

    computeAccessible0: function(/* Board */ b, /* Square */ s,
            /* int */ dx, /* int */ dy, /* int */ limit) {
        var x = s.x();
        var y = s.y();
        
        while (limit-- > 0) {
            x = String.fromCharCode(x.charCodeAt(0)+dx);
            y += dy;
            var next = b.findSquare0(x, y);
            if (next === null) {
                break;
            }
            if (next.pieceColor() === s.pieceColor()) {
                break;
            }
            next.accessible(true);
            if (next.pieceColor() !== null) {
                break;
            }
        }
    },
    
    pawns: function(/* Board */ b, /* Square */ s) {
        var white = (s.pieceColor() === Color.WHITE);
        var dy = white ? 1 : -1;
        var step = b.findSquare0(s.x(), s.y() + dy);
        if ((step !== null) && (step.piece() === null)) {
            step.accessible(true);
            if ((s.y() === 2 && white) || (s.y() === 7 && !white)) {
                var nextSTep = b.findSquare0(s.x(), step.y() + dy);
                if (nextSTep !== null && step.piece() === null && nextSTep.piece() === null) {
                    nextSTep.accessible(true);
                }
            }
        }
        var opposite = white ? Color.BLACK : Color.WHITE;
        var takeLeft = b.findSquare0(String.fromCharCode(s.x().charCodeAt(0)-1), s.y() + dy);
        if (takeLeft !== null && takeLeft.pieceColor() === opposite) {
            takeLeft.accessible(true);
        }
        var takeRight = b.findSquare0(String.fromCharCode(s.x().charCodeAt(0)+1), s.y() + dy);
        if (takeRight !== null && takeRight.pieceColor() === opposite) {
            takeRight.accessible(true);
        }
        if ((white && s.y() === 5) || (!white && s.y() === 4)) {
            var enPassantFrom = white ? 7 : 2;
            var enPassantTo = white ? 5 : 4;
            if (b.moves().length !== 0) {
                var last = b.moves()[b.moves().length - 1];
                if (
                    last.piece() === PieceType.PAWN &&
                    last.from().y() === enPassantFrom &&
                    last.to().y() === enPassantTo
                ) {
                    if (takeLeft !== null && last.from().x() === String.fromCharCode(s.x().charCodeAt(0)-1)) {
                        takeLeft.accessible(true);
                    }
                    if (takeRight !== null && last.from().x() === String.fromCharCode(s.x().charCodeAt(0)+1)) {
                        takeRight.accessible(true);
                    }
                }
            }
        }
    },

    createBoard: function() {
        var b = new Board();
        this.initBoardField(b);
        this.initBoard(b);
        return b;
    },

    initBoardField: function(/* Board */ b) {
        for (var i = 8; i > 0; i--) {
            var arr = [];
            for (var j = 'A'; j <= 'H'; j = String.fromCharCode(j.charCodeAt(0)+1)) {
                var square = new Square();
                var position = new Position();
                position.x(j);
                position.y(i);
                square.position(position);
                square.color(((i + j.charCodeAt(0)) % 2 === 1) ? Color.WHITE : Color.BLACK);
                arr[j.charCodeAt(0) - 'A'.charCodeAt(0)] = square;
            }
            var row = new Row();
            row.columns(arr);
            b.rows.push(row);
        }
    },
    
    initBoard: function(/* Board */ b) {
        this.initBoard0(b, true);
    },

    initBoard0: function(/* Board */ b, /* boolean */ init) {
        if (init) {
            b.turn(Color.WHITE);
        }
        if (b.rows().length === 0) {
            for (var i = 8; i > 0; i--) {
                var r = b.rows()[8 - i];
                for (var j = 'A'; j <= 'H'; j = String.fromCharCode(j.charCodeAt(0)+1)) {
                    var s = r.columns()[j.charCodeAt(0) - 'A'.charCodeAt(0)];
                    s.accessible(false);
                    s.pending(false);
                    r.columns()[j.charCodeAt(0) - 'A'.charCodeAt(0)](s);
                    this.initialPosition(s, init);
                }
            }
        } else {
            var rows = b.rows();
            for (var i=0; i<rows.length; i++) {
                var r = rows[i];
                var columns = r.columns();
                for (var j=0; j<columns.length; j++) {
                    var square = columns[j];
                    square.accessible(false);
                    square.pending(false);
                    square.piece(null);
                    square.pieceColor(null);
                    square.selected(false);
                    this.initialPosition(square, init);
                }
            }
        }
        b.pendingMove(null);
    },

    initialPosition: function(/* Square */ s, /* boolean */ init) {
        var row = s.position().y();
        var column = s.position().x();
        s.piece(null);
        s.pieceColor(null);
        if (init) {
            if (row === 2) {
                s.piece(PieceType.PAWN);
                s.pieceColor(Color.WHITE);
            } else if (row === 7) {
                s.piece(PieceType.PAWN);
                s.pieceColor(Color.BLACK);
            } else if (row === 8 || row === 1) {
                s.pieceColor(row === 1 ? Color.WHITE : Color.BLACK);
                var t;
                switch (column) {
                    case 'A':
                    case 'H':
                        t = PieceType.ROCK;
                        break;
                    case 'B':
                    case 'G':
                        t = PieceType.KNIGHT;
                        break;
                    case 'C':
                    case 'F':
                        t = PieceType.BISHOP;
                        break;
                    case 'D':
                        t = PieceType.QUEEN;
                        break;
                    default:
                        t = PieceType.KING;
                        break;
                }
                s.piece(t);
            }
        }
    },

    initBoard1: function(/* Board */ board, /* String[] */ whites, /* String[] */ blacks, /* Color */ turn) {
        this.initBoard0(board, false);
        for (var i=0; i<whites.length; i++) {
            var w = whites[i];
            if (w.length !== 3) {
                throw ('Expecting three letter string: ' + w);
            }
            w = w.toUpperCase();
            var column = w.charAt(1);
            var row = w.charCodeAt(2) - '0'.charCodeAt(0);
            
            var s = board.findSquare0(column, row);
            s.pieceColor(Color.WHITE);
            s.piece(PieceType.fromNotation(w.charAt(0)));
        }
        for (var i=0; i<blacks.length; i++) {
            var w = blacks[i];
            if (w.length !== 3) {
                throw ('Expecting three letter string: ' + w);
            }
            w = w.toUpperCase();
            var column = w.charAt(1);
            var row = w.charCodeAt(2) - '0'.charCodeAt(0);

            var s = board.findSquare0(column, row);
            s.pieceColor(Color.BLACK);
            s.piece(PieceType.fromNotation(w.charAt(0)));
        }
        board.turn(turn);
    },

    checkRochade: function(/* Board */ b, /* Square */ s) {
        if (s.position().x() === 'E') {
            var y = s.position().y();
            var gRow = b.findSquare0('G', y);
            if (b.findSquare0('H', y).piece() === PieceType.ROCK 
                    && b.findSquare0('F', y).piece() === null
                    && gRow.piece() === null) {
                gRow.accessible(true);
            }
            var cRow = b.findSquare0('C', y);
            if (b.findSquare0('A', y).piece() === PieceType.ROCK 
                    && b.findSquare0('B', y).piece() === null
                    && b.findSquare0('D', y).piece() === null
                    && cRow.piece() === null) {
                cRow.accessible(true);
            }
        }
    }

};

UI = function() {
    var self = this;

    self.status = ko.observable(); // String
    self.selectedGameId = ko.observable(null); // String
    self.boards = ko.observableArray(); // Board[]
    self.viewGames = ko.observable(new Games()); // Games
    self.settings = ko.observable(new Settings()); // Settings
    self.connected = ko.observable(); // boolean
    self.disconnectionTrigger = ko.observable(); // String

    self.disconnected = ko.computed(function() {
        return !self.connected();
    });

    self.settingsActive = ko.computed(function() {
        return (self.selectedGameId() === null);
    });
 
    self.viewGamesActive = ko.computed(function() {
        return (self.selectedGameId() === null) && self.connected();
    });

    self.selectedBoard = ko.computed(function() {
        var active = null;
        var boards = self.boards();
        for (var i=0; i<boards.length; i++) {
            var board = boards[i];
            if (self.selectedGameId() === board.gameId()) {
                board.active(true);
                active = board;
            } else {
                board.active(false);
            }
        }
        return active;
    });

    self.activateGame = function(/* Board */ data) {
        self.selectedGameId(data.gameId());
    };

    self.activateSettings = function() {
        self.selectedGameId(null);
        self.refreshGames();
    };

    self.createGame = function() {
        self.status('Creating a new game...');
        var r = new Request();
        r.msg(MsgType.CreateGame);
        r.color(self.viewGames().selectedColor());
        self.sendMsg(r);
    };

    self.refreshGames = function() {
        self.status('Refreshing games...');
        var r = new Request();
        r.msg(MsgType.QueryGames);
        self.sendMsg(r);
    };

    self.sendMsg = function(r) {
        var sttngs = self.settings();
        var url = sttngs.url();
        r.username(sttngs.username());
        r.password(sttngs.password());
        self.queryServer(url, r);
    };

    self.socket = null;

    self.queryServer = function(url, data) {
        if (self.socket === null) {
            console.log('Connecting to ' + url);
            self.socket = new WebSocket(url);
            self.socket.onopen = function() {
                console.log('WebSocket is open.');
                self.queryServer0(null);
            };
            self.socket.onerror = function(error) {
                console.log('WebSocket error: ' + JSON.stringify(error));
                self.socket = null;
                self.wasAnError(error.type);
            };
            self.socket.onmessage = function(e) {
                console.log('Received message: ' + e.data);
                self.queryServer0(JSON.parse(e.data)); 
            };
            self.socket.onclose = function() {
                console.log('WebSocket closed.');
                self.socket = null;
                self.wasAnError(null);
            };
        } else {
            if (data === null) {
                self.socket.close();
            } else {
                var message = ko.toJSON(data);
                console.log('Sending message: ' + message);
                self.socket.send(message);
            }
        }
    };

    self.queryServer0 = function(r) {
        if (r === null) {
            self.verifyLogin();
            return;
        }
        switch (MsgType.forResponse(r.msg)) {
            case MsgType.QueryMoves:
                var board = self.findBoard(r.gameId);
                if (board) {
                    var selected = board.findSelectedSquare();
                    if (selected && selected.position().location() === r.from) {
                        board.updateAccessible(r.moves);
                    }
                }
                break;
            case MsgType.CheckCredentials:
                switch (r.check) {
                    case 'NOT_REGISTERED':
                    case 'VALID':
                        self.connected(true);
                        self.refreshGames();
                        self.status('Connected.');
                        return;
                    case 'INVALID':
                        self.disconnect('Name or password is invalid!');
                        break;
                    default:
                        self.disconnect();
                        break;
                }
                break;
            case MsgType.QueryGames:
                self.viewGames().games.removeAll();
                for (var i=0; i<r.games.length; i++) {
                    var gameJS = r.games[i];
                    var game = new Game(gameJS);
                    var username = self.settings().username();
                    game.own((game.blackPlayer() === username || game.whitePlayer() === username));
                    self.viewGames().games.push(game);
                }
                self.status('');
                break;
            case MsgType.CreateGame: {
                self.status('Game ' + JSON.stringify(r) + ' created');
                var b = Rules.createBoard();
                Rules.initBoard(
                    b, r.board.whites,
                    r.board.blacks,
                    r.turn
                );
                b.gameId(r.gameId);
                b.player(r.color);
                self.boards.push(b);
                self.selectedGameId(r.gameId);
                self.refreshGames();
                break;
            }
            case MsgType.SendMove: {
                if (!r.board) {
                    throw ('No board in ' + JSON.stringify(r));
                }
                var b = self.findBoard(r.gameId);
                if (b === null) {
                    break;
                }
                var errMsg = r.error ? r.error.message : null;
                var whites = r.board.whites;
                var blacks = r.board.blacks;
                var turn = self.nextTurn(r);
                var alert = r.alert ? new Alert(r.alert) : null;
                b.moveResponse(errMsg, whites, blacks, turn, alert);
                break;
            }
            case MsgType.JoinGame: {
                if (self.findBoard(r.gameId)) {
                    self.selectedGameId(r.gameId);
                    break;
                }
                self.status('Joining ' + r.gameId + ' as ' + r.color);
                var b = Rules.createBoard();
                Rules.initBoard1(
                    b, r.board.whites,
                    r.board.blacks,
                    r.turn);
                b.gameId(r.gameId);
                b.player(r.color);
                if (b.player() === Color.BLACK) {
                    b.rotateBoard();
                }
                if (r.moves) {
                    for (var i=0; i<r.moves.length; i++) {
                        var move = r.moves[i];
                        var m = Move.valueOf(move);
                        b.moves.push(m);
                    }
                } else {
                    b.moves.removeAll();
                }
                self.selectedGameId(b.gameId());
                self.boards.push(b);
                break;
            }
            case MsgType.UpdateGame: {
                var b = self.findBoard(r.gameId);
                if (b === null) {
                    break;
                }
                var move = Move.valueOf(r.from + r.to);
                var whites = r.board.whites;
                var blacks = r.board.blacks;
                var turn = self.nextTurn(r);
                var alert = r.alert ? new Alert(r.alert) : null;
                b.moveUpdate(move, whites, blacks, turn, alert);
            }
        }
        
        self.nextTurn = function(r) {
            var alert = r.alert;
            if (!alert || !AlertType.terminal(alert.type)) {
                return r.turn;
            } else {
                return null;
            }
        };
    };

    self.findBoard = function(gameId) {
        var boards = self.boards();
        for (var i=0; i<boards.length; i++) {
            var tmp = boards[i];
            if (tmp.gameId() === gameId) {
                return tmp;
            }
        }
    };

    self.wasAnError = function(t) {
        if (t === null) {
            self.connected(false);
            var status = 'Disconnected.';
            var trigger = self.disconnectionTrigger();
            if (trigger) {
                status = trigger + ' ' + status;
            }
            self.disconnectionTrigger(null);
            self.status(status);
        } else {
            self.status('Error: ' + t);
        }
    };
    
    self.joinGame = function(/* Game */ data) {
        var r = new Request();
        r.msg(MsgType.JoinGame);
        r.observer(false);
        r.gameId(data.gameId());
        self.sendMsg(r);
    };

    self.observeGame = function(/* Game */ data) {
        var r = new Request();
        r.msg(MsgType.JoinGame);
        r.observer(true);
        r.gameId(data.gameId());
        self.sendMsg(r);
    };

    self.connected.subscribe(function() {
        if (self.disconnected()) {
            self.boards.removeAll();
            var index = UI.ACTIVE.indexOf(self);
            if (index !== -1) {
                UI.ACTIVE.splice(index, 1);
            }
        } else {
            UI.ACTIVE.push(self);
        }
    });

    self.leave = function(/* Board */ data) {
        self.boards.remove(data);
        self.activateSettings();
    };

    self.disconnect = function(trigger) {
        if (typeof(trigger) !== 'string') {
            trigger = null;
        }
        self.disconnectionTrigger(trigger);
        var status = 'Disconnecting...';
        if (trigger) {
            status = trigger + ' ' + status;
        }
        self.status(status);
        self.queryServer(self.settings().url(), null);
    };

    self.verifyLogin = function() {
        self.status('Verifying user credentials');
        var r = new Request();
        r.msg(MsgType.CheckCredentials);
        self.sendMsg(r);
    };

    self.reconnect = function() {
        self.status('Connecting to the server...');
        self.queryServer(self.settings().url(), null);
        self.settings().write();
    };

};

UI.ACTIVE = [];

UI.findUI = function(/* Board */ b) {
    for (var i=0; i<this.ACTIVE.length; i++) {
        var ui = this.ACTIVE[i];
        if (ui.boards().indexOf(b) !== -1) {
            return ui;
        }
    }
    return null;
};

var model = new UI();
model.settings().read();
model.status('Ready.');
ko.applyBindings(model);
