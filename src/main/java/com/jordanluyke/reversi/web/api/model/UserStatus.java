package com.jordanluyke.reversi.web.api.model;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@ToString
public class UserStatus {
    private static final int offlineChecks = 15;

    private Status status = Status.ACTIVE;
    private Subject<Status> onChange = PublishSubject.create();
    private int offlineChecksRemaining = offlineChecks;

    public void reset() {
        status = Status.ACTIVE;
        onChange.onNext(Status.ACTIVE);
        offlineChecksRemaining = offlineChecks;
    }

    public enum Status {
        ACTIVE,
        DISCONNECTED,
        OFFLINE
    }
}
