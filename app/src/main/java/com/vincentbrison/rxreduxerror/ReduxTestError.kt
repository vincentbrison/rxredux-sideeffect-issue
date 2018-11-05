package com.vincentbrison.rxreduxerror

import android.util.Log
import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import kotlin.reflect.KFunction2

fun testCrashingSideEffectWontCrashApp1() {
    val sideEffects = listOf(::crashingSideEffect)
    createStream(sideEffects)
}

fun testCrashingSideEffectWontCrashApp2() {
    val sideEffects = listOf(::crashingSideEffect, ::notCrashingSideEffect)
    createStream(sideEffects)
}

private fun createStream(
    sideEffects: List<KFunction2<Observable<Action>, StateAccessor<State>, Observable<Action>>>
) {
    PublishProcessor
        .create<Action>()
        .startWith(Action)
        .toObservable()
        .reduxStore(State(), sideEffects, ::reducer)
        .distinctUntilChanged()
        .subscribe({ Log.d("", "onNext $it") }, { Log.e("", "onError $it") })
}

class State

object Action

private fun reducer(state: State, action: Action): State = state

private fun crashingSideEffect(actions: Observable<Action>, state: StateAccessor<State>): Observable<Action> {
    return actions.map { throw NumberFormatException() }.flatMap { Observable.empty<Action>() }
}

private fun notCrashingSideEffect(actions: Observable<Action>, state: StateAccessor<State>): Observable<Action> {
    return actions.flatMap { Observable.empty<Action>() }
}